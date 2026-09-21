package com.nullpointer.nourseCompose.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepository
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
) : ViewModel() {
    val reminders = repository.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    fun save(reminder: MedicationReminderEntity) = viewModelScope.launch {
        if (reminder.id == 0L) scheduler.schedule(reminder.copy(id = repository.add(reminder))) else { repository.update(reminder); scheduler.schedule(reminder) }
    }

    fun setActive(reminder: MedicationReminderEntity, active: Boolean) = viewModelScope.launch {
        val updated = reminder.copy(isActive = active)
        repository.update(updated)
        if (active) scheduler.schedule(updated) else scheduler.cancel(updated.id)
    }

    fun delete(reminder: MedicationReminderEntity) = viewModelScope.launch {
        scheduler.cancel(reminder.id)
        repository.delete(reminder)
    }
}
