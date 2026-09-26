package com.nullpointer.nourseCompose.domain.alarm

import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import kotlinx.coroutines.flow.Flow

interface AlarmLogRepository {
    fun observeAll(): Flow<List<AlarmLogEntity>>
    suspend fun record(log: AlarmLogEntity)
    suspend fun deleteAll()
}
