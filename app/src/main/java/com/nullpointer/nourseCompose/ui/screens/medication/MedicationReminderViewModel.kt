package com.nullpointer.nourseCompose.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepository
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import com.nullpointer.nourseCompose.domain.alarm.AppLogger
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.notifications.MedicationReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationReminderViewModel @Inject constructor(
    private val repository: MedicationReminderRepository,
    private val scheduler: MedicationReminderScheduler,
    private val alarmLogRepository: AlarmLogRepository,
    private val appLogger: AppLogger,
) : ViewModel() {
    val reminders = repository.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    fun save(reminder: MedicationReminderEntity) = viewModelScope.launch {
        runCatching {
            if (reminder.id == 0L) {
                val isFirst = reminders.value.isEmpty()
                val id = repository.add(reminder)
                val saved = reminder.copy(id = id)
                scheduler.schedule(saved)
                alarmLogRepository.record(AlarmLogEntity(reminderId = id, reminderName = saved.name, eventType = AlarmLogEvent.REMINDER_CREATED, success = true, details = if (isFirst) "First reminder registered" else "Reminder registered", isFirstReminder = isFirst))
            } else {
                repository.update(reminder)
                scheduler.schedule(reminder)
                alarmLogRepository.record(AlarmLogEntity(reminderId = reminder.id, reminderName = reminder.name, eventType = AlarmLogEvent.REMINDER_UPDATED, success = true, details = "Reminder updated"))
            }
        }.onFailure { appLogger.error("Medication reminder save", it.message ?: "Save failed", it) }
    }

    fun setActive(reminder: MedicationReminderEntity, active: Boolean) = viewModelScope.launch {
        val updated = reminder.copy(isActive = active)
        repository.update(updated)
        if (active) {
            scheduler.schedule(updated)
            alarmLogRepository.record(AlarmLogEntity(reminderId = updated.id, reminderName = updated.name, eventType = AlarmLogEvent.REMINDER_ENABLED, success = true, details = "Reminder enabled"))
        } else {
            scheduler.cancel(updated.id)
            alarmLogRepository.record(AlarmLogEntity(reminderId = updated.id, reminderName = updated.name, eventType = AlarmLogEvent.REMINDER_DISABLED, success = true, details = "Reminder cancelled"))
        }
    }

    fun delete(reminder: MedicationReminderEntity) = viewModelScope.launch {
        scheduler.cancel(reminder.id)
        repository.delete(reminder)
        alarmLogRepository.record(AlarmLogEntity(reminderId = reminder.id, reminderName = reminder.name, eventType = AlarmLogEvent.REMINDER_DELETED, success = true, details = "Reminder deleted and alarm cancelled"))
    }
}
