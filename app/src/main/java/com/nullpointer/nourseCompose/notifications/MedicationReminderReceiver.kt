package com.nullpointer.nourseCompose.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepository
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject

@AndroidEntryPoint
class MedicationReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: MedicationReminderRepository
    @Inject lateinit var scheduler: MedicationReminderScheduler
    @Inject lateinit var alarmLogRepository: AlarmLogRepository
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == MedicationReminderScheduler.ACTION_SNOOZE) {
            scheduler.snooze(intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1)); return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == MedicationReminderScheduler.ACTION_REMINDER) {
                    val reminderId = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1)
                    val reminder = repository.observeAll().first().firstOrNull { it.id == reminderId }
                    if (reminder == null) {
                        alarmLogRepository.record(AlarmLogEntity(reminderId = reminderId, reminderName = "Unknown", eventType = AlarmLogEvent.ALARM_FAILED, success = false, details = "Reminder not found", severity = "ERROR"))
                    } else {
                        val notificationResult = runCatching { scheduler.showNotification(reminder) }
                        val result = notificationResult.getOrElse {
                            NotificationDispatchResult(
                                posted = false,
                                details = "type=MEDICATION; notificationPosted=false; dispatchException=${it.javaClass.simpleName}: ${it.message}",
                            )
                        }
                        val shown = result.posted
                        val details = notificationResult.exceptionOrNull()?.message
                            ?: result.details
                        alarmLogRepository.record(AlarmLogEntity(reminderId = reminder.id, reminderName = reminder.name, eventType = if (shown) AlarmLogEvent.ALARM_LAUNCHED else AlarmLogEvent.ALARM_FAILED, success = shown, details = details, severity = if (shown) "INFO" else "ERROR"))
                        if (shown && reminder.fullScreenAlarm &&
                            ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                        ) {
                            alarmLogRepository.record(AlarmLogEntity(reminderId = reminder.id, reminderName = reminder.name, eventType = AlarmLogEvent.FOREGROUND_ALARM_ACTIVITY_REQUESTED, success = true, details = "Opening alarm activity because NurseApp is foreground"))
                            withContext(Dispatchers.Main) {
                                context.startActivity(
                                    Intent(context, MedicationAlarmActivity::class.java)
                                        .putExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, reminder.id)
                                        .putExtra("reminder_name", reminder.name)
                                        .putExtra("reminder_dosage", reminder.dosage)
                                        .putExtra("reminder_photo", reminder.photoUri)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                                )
                            }
                        }
                        scheduler.schedule(reminder)
                    }
                } else repository.observeActive().first().forEach(scheduler::schedule)
            } finally { pendingResult.finish() }
        }
    }
}
