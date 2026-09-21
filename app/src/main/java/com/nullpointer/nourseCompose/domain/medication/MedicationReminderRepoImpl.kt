package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.datasource.medication.local.MedicationReminderLocalDataSource
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.Flow

class MedicationReminderRepoImpl(
    private val localDataSource: MedicationReminderLocalDataSource,
) : MedicationReminderRepository {
    override fun observeAll(): Flow<List<MedicationReminderEntity>> = localDataSource.observeAll()

    override fun observeActive(): Flow<List<MedicationReminderEntity>> = localDataSource.observeActive()

    override suspend fun add(reminder: MedicationReminderEntity): Long = localDataSource.add(reminder)

    override suspend fun update(reminder: MedicationReminderEntity) = localDataSource.update(reminder)

    override suspend fun delete(reminder: MedicationReminderEntity) = localDataSource.delete(reminder)
}