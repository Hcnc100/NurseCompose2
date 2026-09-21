package com.nullpointer.nourseCompose.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nullpointer.nourseCompose.MainActivity
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.medication.ReminderSchedule
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
        ?: error("AlarmManager is not available")

    fun schedule(reminder: MedicationReminderEntity) {
        if (!reminder.isActive) return
        val now = System.currentTimeMillis()
        val triggerAt = ReminderSchedule.occurrencesBetween(reminder, now, now + reminder.intervalHours * 60L * 60L * 1_000L + 60_000L).firstOrNull() ?: return
        val pendingIntent = reminderPendingIntent(reminder.id)
        alarmManager.cancel(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && reminder.useExactAlarm && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
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

    fun showNotification(reminder: MedicationReminderEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val contentIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val fullScreenIntent = PendingIntent.getActivity(context, reminder.id.toInt(), Intent(context, MedicationAlarmActivity::class.java).putExtra(EXTRA_REMINDER_ID, reminder.id).putExtra("reminder_name", reminder.name).putExtra("reminder_dosage", reminder.dosage).putExtra("reminder_photo", reminder.photoUri), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val channelId = channelId(reminder)
        ensureChannel(channelId, reminder)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_medication_title, reminder.name))
            .setContentText(reminder.dosage ?: context.getString(R.string.notification_medication_take_now))
            .setContentIntent(contentIntent).setAutoCancel(true)
        if (reminder.vibrationEnabled) builder.setVibrate(longArrayOf(0, 500, 250, 500))
        if (reminder.soundEnabled) builder.setDefaults(android.app.Notification.DEFAULT_SOUND)
        if (reminder.fullScreenAlarm) builder.setFullScreenIntent(fullScreenIntent, true)
        NotificationManagerCompat.from(context).notify(reminder.id.toInt(), builder.build())
    }

    private fun channelId(reminder: MedicationReminderEntity) = "medication_${if (reminder.soundEnabled) "sound" else "silent"}_${if (reminder.vibrationEnabled) "vibrate" else "still"}_${if (reminder.fullScreenAlarm) "alarm" else "notice"}"
    private fun ensureChannel(id: String, reminder: MedicationReminderEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.getSystemService(context, NotificationManager::class.java)?.createNotificationChannel(NotificationChannel(id, context.getString(R.string.notification_channel_medications), if (reminder.soundEnabled || reminder.vibrationEnabled || reminder.fullScreenAlarm) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_LOW).apply { description = context.getString(R.string.notification_channel_medications_description); enableVibration(reminder.vibrationEnabled); if (!reminder.soundEnabled) setSound(null, null) })
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
