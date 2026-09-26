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
                        val shown = notificationResult.getOrDefault(false)
                        val details = notificationResult.exceptionOrNull()?.message
                            ?: if (shown) "Notification dispatched" else "Notification permission disabled"
                        alarmLogRepository.record(AlarmLogEntity(reminderId = reminder.id, reminderName = reminder.name, eventType = if (shown) AlarmLogEvent.ALARM_LAUNCHED else AlarmLogEvent.ALARM_FAILED, success = shown, details = details, severity = if (shown) "INFO" else "ERROR"))
                        scheduler.schedule(reminder)
                    }
                } else repository.observeActive().first().forEach(scheduler::schedule)
            } finally { pendingResult.finish() }
        }
    }
}
