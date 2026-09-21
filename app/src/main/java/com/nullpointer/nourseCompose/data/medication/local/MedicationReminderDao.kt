package com.nullpointer.nourseCompose.data.medication.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationReminderDao {
    @Query("SELECT * FROM medication_reminders ORDER BY startAt ASC")
    fun observeAll(): Flow<List<MedicationReminderEntity>>

    @Query("SELECT * FROM medication_reminders WHERE isActive = 1 ORDER BY startAt ASC")
    fun observeActive(): Flow<List<MedicationReminderEntity>>

    @Insert
    suspend fun insert(reminder: MedicationReminderEntity): Long

    @Update
    suspend fun update(reminder: MedicationReminderEntity)

    @Delete
    suspend fun delete(reminder: MedicationReminderEntity)
}