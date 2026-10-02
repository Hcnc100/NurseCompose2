package com.nullpointer.nourseCompose.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CancellationException
import com.nullpointer.nourseCompose.domain.medication.ReminderSaveSession
import javax.inject.Inject

@HiltViewModel
class MedicationReminderViewModel @Inject constructor(
    private val repository: MedicationReminderRepository,
    private val scheduler: MedicationReminderScheduler,
    private val alarmLogRepository: AlarmLogRepository,
    private val appLogger: AppLogger,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val nextAlarmTimes = scheduler.nextAlarmTimes
    enum class SaveStatus { IDLE, SAVING, SAVED, ERROR }
    private val _saveStatus = MutableStateFlow(if (savedStateHandle.get<Boolean>("editor_save_completed") == true) SaveStatus.SAVED else SaveStatus.IDLE)
    val saveStatus = _saveStatus.asStateFlow()
    val reminderData: StateFlow<List<MedicationReminderEntity>?> = repository.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )
    val reminders = reminderData.map { it.orEmpty() }.stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5_000), emptyList())
    private val saveSession = ReminderSaveSession(repository, scheduler::schedule, record = { saved, created, first ->
        alarmLogRepository.record(AlarmLogEntity(reminderId = saved.id, reminderName = saved.name,
            eventType = if (created) AlarmLogEvent.REMINDER_CREATED else AlarmLogEvent.REMINDER_UPDATED,
            success = true, details = if (created) "Reminder registered" else "Reminder updated",
            isFirstReminder = created && first))
    }, initialPersistedId = savedStateHandle["pending_new_reminder_id"],
        initialFirstReminder = savedStateHandle["pending_first_reminder"] ?: false,
        onPersisted = { id, first ->
            savedStateHandle["pending_new_reminder_id"] = id
            savedStateHandle["pending_first_reminder"] = first
        })

    fun save(reminder: MedicationReminderEntity) = viewModelScope.launch {
        if (_saveStatus.value == SaveStatus.SAVING || _saveStatus.value == SaveStatus.SAVED) return@launch
        _saveStatus.value = SaveStatus.SAVING
        runCatching {
            saveSession.save(reminder)
        }.onSuccess {
            savedStateHandle["editor_save_completed"] = true
            _saveStatus.value = SaveStatus.SAVED
        }.onFailure {
            if (it is CancellationException) throw it
            _saveStatus.value = SaveStatus.ERROR
            appLogger.error("Medication reminder save", it.message ?: "Save failed", it)
        }
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
