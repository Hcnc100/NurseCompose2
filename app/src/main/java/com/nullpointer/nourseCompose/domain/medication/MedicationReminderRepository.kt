package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.Flow

interface MedicationReminderRepository {
    fun observeAll(): Flow<List<MedicationReminderEntity>>
    fun observeActive(): Flow<List<MedicationReminderEntity>>
    suspend fun add(reminder: MedicationReminderEntity): Long
    suspend fun update(reminder: MedicationReminderEntity)
    suspend fun delete(reminder: MedicationReminderEntity)
}