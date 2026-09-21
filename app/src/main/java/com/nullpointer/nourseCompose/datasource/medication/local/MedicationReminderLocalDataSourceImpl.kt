package com.nullpointer.nourseCompose.datasource.medication.local

import com.nullpointer.nourseCompose.data.medication.local.MedicationReminderDao
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.Flow

class MedicationReminderLocalDataSourceImpl(
    private val medicationReminderDao: MedicationReminderDao,
) : MedicationReminderLocalDataSource {
    override fun observeAll(): Flow<List<MedicationReminderEntity>> = medicationReminderDao.observeAll()

    override fun observeActive(): Flow<List<MedicationReminderEntity>> = medicationReminderDao.observeActive()

    override suspend fun add(reminder: MedicationReminderEntity): Long = medicationReminderDao.insert(reminder)

    override suspend fun update(reminder: MedicationReminderEntity) = medicationReminderDao.update(reminder)

    override suspend fun delete(reminder: MedicationReminderEntity) = medicationReminderDao.delete(reminder)
}