package com.nullpointer.nourseCompose.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nullpointer.nourseCompose.MainActivity
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.medication.ReminderSchedule
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

data class NotificationDispatchResult(
    val posted: Boolean,
    val details: String,
)

@Singleton
class MedicationReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmLogRepository: AlarmLogRepository,
) {
    private val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
        ?: error("AlarmManager is not available")

    fun schedule(reminder: MedicationReminderEntity) {
        if (!reminder.isActive) return
        val now = System.currentTimeMillis()
        val triggerAt = ReminderSchedule.occurrencesBetween(reminder, now, now + reminder.intervalMinutes * 60L * 1_000L + 60_000L).firstOrNull()
            ?: return
        val pendingIntent = reminderPendingIntent(reminder.id)
        alarmManager.cancel(pendingIntent)
        val exactPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        val exactRequested = reminder.useExactAlarm || exactPermission
        val useAlarmClock = reminder.fullScreenAlarm && exactPermission
        val scheduledExact = runCatching {
            if (useAlarmClock) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    reminder.id.toInt(),
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAt, showIntent),
                    pendingIntent,
                )
                true
            } else if (exactRequested && exactPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                else alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                true
            } else false
        }.getOrElse { false }
        if (!scheduledExact) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            else alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
        CoroutineScope(Dispatchers.IO).launch {
            alarmLogRepository.record(
                AlarmLogEntity(
                    reminderId = reminder.id,
                    reminderName = reminder.name,
                    eventType = AlarmLogEvent.ALARM_SCHEDULED,
                    success = true,
                    details = "Trigger: ${DateFormat.getDateTimeInstance().format(Date(triggerAt))}; scheduleType=${if (useAlarmClock) "ALARM_CLOCK" else "STANDARD"}; exact=$scheduledExact; exactPermission=$exactPermission",
                )
            )
        }
    }

    fun cancel(reminderId: Long) = alarmManager.cancel(reminderPendingIntent(reminderId))
    fun snooze(reminderId: Long, minutes: Long = 10) {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        val pendingIntent = reminderPendingIntent(reminderId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun showNotification(reminder: MedicationReminderEntity): NotificationDispatchResult {
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val postPermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val deliveryMode = if (reminder.fullScreenAlarm) "FULL_SCREEN_ALARM" else "STANDARD_NOTIFICATION"
        if (!postPermissionGranted || !notificationsEnabled) {
            return NotificationDispatchResult(
                posted = false,
                details = "type=MEDICATION; deliveryMode=$deliveryMode; notificationPosted=false; postNotificationsPermission=$postPermissionGranted; notificationsEnabled=$notificationsEnabled; reason=${if (!postPermissionGranted) "POST_NOTIFICATIONS denied" else "notifications disabled"}",
            )
        }
        val contentIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val fullScreenIntent = PendingIntent.getActivity(context, reminder.id.toInt(), Intent(context, MedicationAlarmActivity::class.java).putExtra(EXTRA_REMINDER_ID, reminder.id).putExtra("reminder_name", reminder.name).putExtra("reminder_dosage", reminder.dosage).putExtra("reminder_photo", reminder.photoUri), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val channelId = channelId(reminder)
        ensureChannel(channelId, reminder)
        val channelDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = ContextCompat.getSystemService(context, NotificationManager::class.java)
                ?.getNotificationChannel(channelId)
            "channelId=$channelId; channelImportance=${channel?.importance ?: "unknown"}; channelSound=${channel?.sound != null}; channelVibration=${channel?.shouldVibrate() ?: reminder.vibrationEnabled}"
        } else {
            "channelId=$channelId; channelImportance=pre-O; channelSound=${reminder.soundEnabled}; channelVibration=${reminder.vibrationEnabled}"
        }
        val fullScreenAccess = when {
            !reminder.fullScreenAlarm -> "not_requested"
            Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> "not_required"
            ContextCompat.getSystemService(context, NotificationManager::class.java)?.canUseFullScreenIntent() == true -> "granted"
            else -> "denied"
        }
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_medication_title, reminder.name))
            .setContentText(reminder.dosage ?: context.getString(R.string.notification_medication_take_now))
            .setContentIntent(contentIntent)
            .setAutoCancel(!reminder.fullScreenAlarm)
            .setPriority(if (reminder.fullScreenAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
        if (reminder.vibrationEnabled) builder.setVibrate(longArrayOf(0, 500, 250, 500))
        if (reminder.soundEnabled) builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        if (reminder.fullScreenAlarm) builder.setFullScreenIntent(fullScreenIntent, true)
        NotificationManagerCompat.from(context).notify(reminder.id.toInt(), builder.build())
        return NotificationDispatchResult(
            posted = true,
            details = "type=MEDICATION; deliveryMode=$deliveryMode; notificationPosted=true; fullScreenIntentAttached=${reminder.fullScreenAlarm}; fullScreenAccess=$fullScreenAccess; postNotificationsPermission=$postPermissionGranted; notificationsEnabled=$notificationsEnabled; $channelDetails; requestedSound=${reminder.soundEnabled}; requestedVibration=${reminder.vibrationEnabled}; priority=${if (reminder.fullScreenAlarm) "MAX" else "HIGH"}; category=ALARM",
        )
    }

    private fun channelId(reminder: MedicationReminderEntity) = "medication_${if (reminder.soundEnabled) "sound" else "silent"}_${if (reminder.vibrationEnabled) "vibrate" else "still"}_${if (reminder.fullScreenAlarm) "alarm" else "notice"}"
    private fun ensureChannel(id: String, reminder: MedicationReminderEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.getSystemService(context, NotificationManager::class.java)?.createNotificationChannel(NotificationChannel(id, context.getString(R.string.notification_channel_medications), if (reminder.soundEnabled || reminder.vibrationEnabled || reminder.fullScreenAlarm) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_LOW).apply {
            description = context.getString(R.string.notification_channel_medications_description)
            enableVibration(reminder.vibrationEnabled)
            setSound(
                if (reminder.soundEnabled) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) else null,
                if (reminder.soundEnabled) AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build() else null
            )
        })
    }

    private fun reminderPendingIntent(id: Long): PendingIntent = PendingIntent.getBroadcast(context, id.hashCode(), Intent(context, MedicationReminderReceiver::class.java).setAction(ACTION_REMINDER).putExtra(EXTRA_REMINDER_ID, id), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val ACTION_REMINDER = "com.nullpointer.nourseCompose.MEDICATION_REMINDER"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val ACTION_SNOOZE = "com.nullpointer.nourseCompose.SNOOZE"
        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.getSystemService(context, NotificationManager::class.java)?.createNotificationChannel(NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel_medications), NotificationManager.IMPORTANCE_LOW).apply { description = context.getString(R.string.notification_channel_medications_description); setSound(null, null) })
        }
    }
}
