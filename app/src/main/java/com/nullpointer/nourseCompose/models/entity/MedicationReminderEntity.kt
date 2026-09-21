package com.nullpointer.nourseCompose.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_reminders")
data class MedicationReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String? = null,
    val comment: String? = null,
    val photoUri: String? = null,
    val startAt: Long,
    val endAt: Long? = null,
    val intervalHours: Int,
    val isActive: Boolean = true,
    val useExactAlarm: Boolean = false,
    val notificationMode: String = "NOTIFICATION",
    val vibrationEnabled: Boolean = false,
    val soundEnabled: Boolean = false,
    val fullScreenAlarm: Boolean = false,
)
