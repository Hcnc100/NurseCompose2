package com.nullpointer.nourseCompose.data.alarm.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmLogDao {
    @Query("SELECT * FROM alarm_logs ORDER BY occurredAt DESC, id DESC")
    fun observeAll(): Flow<List<AlarmLogEntity>>

    @Insert
    suspend fun insert(log: AlarmLogEntity)

    @Query("DELETE FROM alarm_logs")
    suspend fun deleteAll()
}
