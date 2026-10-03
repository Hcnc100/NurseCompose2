package com.nullpointer.nourseCompose.medication

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.nullpointer.nourseCompose.database.NurseDatabase
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepoImpl
import com.nullpointer.nourseCompose.domain.alarm.MedicationHistory
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class MedicationResponseCorrectionTest {
    private val database = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext, NurseDatabase::class.java).build()
    private val dao = database.getAlarmLogDao()
    private val repository = AlarmLogRepoImpl(dao)
    private fun response(id: Long = 1, event: String = AlarmLogEvent.MEDICATION_TAKEN) =
        AlarmLogEntity(id = id, reminderId = 50, reminderName = "Prueba", eventType = event,
            occurredAt = 1234567, success = true, details = "Original response")

    @After fun close() { database.close() }

    @Test fun correctionPreservesDateAndCanBeReversedWithAudit() = runBlocking {
        dao.insert(response())
        assertTrue(repository.correctResponse(1, AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN))
        val corrected = dao.findById(1)!!
        assertEquals(1234567L, corrected.occurredAt)
        assertEquals(50L, corrected.reminderId)
        assertEquals(AlarmLogEvent.MEDICATION_NOT_TAKEN, corrected.eventType)
        assertTrue(repository.correctResponse(1, AlarmLogEvent.MEDICATION_NOT_TAKEN, AlarmLogEvent.MEDICATION_TAKEN))
        val all = repository.observeAll().first()
        val audit = all.filter { it.eventType == AlarmLogEvent.MEDICATION_RESPONSE_CORRECTED }
        assertEquals(2, audit.size)
        assertTrue(audit.all { it.details!!.contains("sourceLogId=1") })
        assertEquals(1, MedicationHistory.filter(all, null, null, null).size)
        assertEquals(AlarmLogEvent.MEDICATION_TAKEN, dao.findById(1)!!.eventType)
    }

    @Test fun staleMissingFailedAndNonResponseRecordsCannotBeCorrected() = runBlocking {
        dao.insert(response())
        dao.insert(response(2, AlarmLogEvent.ALARM_DISMISSED))
        dao.insert(response(3).copy(success = false))
        dao.insert(response(4).copy(category = "APP"))
        assertFalse(repository.correctResponse(1, AlarmLogEvent.MEDICATION_NOT_TAKEN, AlarmLogEvent.MEDICATION_TAKEN))
        assertFalse(repository.correctResponse(2, AlarmLogEvent.ALARM_DISMISSED, AlarmLogEvent.MEDICATION_TAKEN))
        assertFalse(repository.correctResponse(3, AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN))
        assertFalse(repository.correctResponse(4, AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN))
        assertFalse(repository.correctResponse(99, AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN))
        assertFalse(repository.correctResponse(1, AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_TAKEN))
        assertFalse(repository.correctResponse(1, AlarmLogEvent.MEDICATION_TAKEN, "UNKNOWN"))
        assertEquals(4, repository.observeAll().first().size)
    }

    @Test fun auditFailureRollsBackResponseChange() = runBlocking {
        dao.insert(response())
        database.openHelper.writableDatabase.execSQL("""
            CREATE TRIGGER reject_qa_audit BEFORE INSERT ON alarm_logs
            WHEN NEW.eventType = 'MEDICATION_RESPONSE_CORRECTED'
            BEGIN SELECT RAISE(ABORT, 'QA audit insert failure'); END
        """.trimIndent())
        val failed = runCatching {
            repository.correctResponse(1, AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN)
        }.isFailure
        assertTrue(failed)
        assertEquals(response(), dao.findById(1))
        assertEquals(1, repository.observeAll().first().size)
    }
}
