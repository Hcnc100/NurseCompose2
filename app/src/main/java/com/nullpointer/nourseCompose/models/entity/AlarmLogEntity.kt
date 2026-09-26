package com.nullpointer.nourseCompose.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_logs")
data class AlarmLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reminderId: Long? = null,
    val reminderName: String,
    val eventType: String,
    val occurredAt: Long = System.currentTimeMillis(),
    val success: Boolean,
    val details: String? = null,
    val isFirstReminder: Boolean = false,
    val category: String = "ALARM",
    val severity: String = "INFO",
    val stackTrace: String? = null,
)
