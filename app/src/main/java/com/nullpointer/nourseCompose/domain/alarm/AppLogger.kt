package com.nullpointer.nourseCompose.domain.alarm

import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val repository: AlarmLogRepository,
) {
    fun error(source: String, message: String, throwable: Throwable? = null) {
        recordBlocking(
            AlarmLogEntity(
                reminderName = source,
                eventType = "APP_ERROR",
                success = false,
                details = message,
                category = "APP",
                severity = "ERROR",
                stackTrace = throwable?.stackTraceToString()?.take(12_000),
            )
        )
    }

    fun recordCrash(threadName: String, throwable: Throwable) {
        error("Crash ($threadName)", throwable.message ?: throwable.javaClass.simpleName, throwable)
    }

    private fun recordBlocking(log: AlarmLogEntity) {
        runCatching {
            runBlocking { withContext(Dispatchers.IO) { repository.record(log) } }
        }
    }
}
