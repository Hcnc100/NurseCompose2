package com.nullpointer.nourseCompose.data.alarm.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmLogDao {
    @Query("SELECT * FROM alarm_logs ORDER BY occurredAt DESC, id DESC")
    fun observeAll(): Flow<List<AlarmLogEntity>>

    @Insert
    suspend fun insert(log: AlarmLogEntity)

    @Query("SELECT * FROM alarm_logs WHERE id = :id")
    suspend fun findById(id: Long): AlarmLogEntity?

    @Query("UPDATE alarm_logs SET eventType = :replacement, details = :details WHERE id = :id AND eventType = :expected AND success = 1 AND category = 'ALARM'")
    suspend fun replaceResponse(id: Long, expected: String, replacement: String, details: String): Int

    /** Corrección y auditoría se guardan juntas; no modifica la fecha de la alarma. */
    @Transaction
    suspend fun correctResponse(id: Long, expectedEvent: String, replacementEvent: String): Boolean {
        val responses = setOf(AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN)
        if (id <= 0 || expectedEvent !in responses || replacementEvent !in responses || expectedEvent == replacementEvent) return false
        val original = findById(id) ?: return false
        if (!original.success || original.category != "ALARM" || original.eventType != expectedEvent) return false
        val updated = replaceResponse(id, expectedEvent, replacementEvent,
            "Response corrected by user; see MEDICATION_RESPONSE_CORRECTED audit events")
        if (updated != 1) return false
        insert(AlarmLogEntity(reminderId = original.reminderId, reminderName = original.reminderName,
            eventType = AlarmLogEvent.MEDICATION_RESPONSE_CORRECTED, success = true,
            details = "sourceLogId=$id; previous=$expectedEvent; corrected=$replacementEvent; previousDetails=${original.details.orEmpty()}"))
        return true
    }

    @Query("DELETE FROM alarm_logs")
    suspend fun deleteAll()
}
