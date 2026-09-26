package com.nullpointer.nourseCompose.domain.alarm

import com.nullpointer.nourseCompose.data.alarm.local.AlarmLogDao
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmLogRepoImpl @Inject constructor(
    private val dao: AlarmLogDao,
) : AlarmLogRepository {
    override fun observeAll(): Flow<List<AlarmLogEntity>> = dao.observeAll()
    override suspend fun record(log: AlarmLogEntity) = dao.insert(log)
    override suspend fun deleteAll() = dao.deleteAll()
}
