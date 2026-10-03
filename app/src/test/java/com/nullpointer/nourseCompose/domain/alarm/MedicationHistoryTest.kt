package com.nullpointer.nourseCompose.domain.alarm

import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class MedicationHistoryTest {
    private fun log(id: Long, time: Long, event: String = AlarmLogEvent.ALARM_LAUNCHED, name: String = "A") =
        AlarmLogEntity(id = id, occurredAt = time, reminderName = name, eventType = event, success = true)

    @Test fun dismissalIsNeverAConfirmation() {
        assertFalse(MedicationHistory.confirmsIntake(log(1, 1, AlarmLogEvent.ALARM_DISMISSED)))
        assertTrue(MedicationHistory.confirmsIntake(log(2, 2, AlarmLogEvent.MEDICATION_TAKEN)))
        assertFalse(MedicationHistory.confirmsIntake(log(3, 3, AlarmLogEvent.MEDICATION_TAKEN).copy(success = false)))
    }
    @Test fun explicitlyNotTakenIsVisibleButNeverConfirmsIntake() {
        val record = log(1, 10, AlarmLogEvent.MEDICATION_NOT_TAKEN)
        assertTrue(MedicationHistory.isVisible(record))
        assertFalse(MedicationHistory.confirmsIntake(record))
        assertEquals(listOf(record), MedicationHistory.filter(listOf(record), null, null, null))
        assertFalse(MedicationHistory.confirmsIntake(record.copy(success = false)))
    }
    @Test fun onlySuccessfulExplicitAlarmResponsesCanBeCorrected() {
        assertTrue(MedicationHistory.isExplicitResponse(log(1, 1, AlarmLogEvent.MEDICATION_TAKEN)))
        assertTrue(MedicationHistory.isExplicitResponse(log(2, 1, AlarmLogEvent.MEDICATION_NOT_TAKEN)))
        assertFalse(MedicationHistory.isExplicitResponse(log(3, 1, AlarmLogEvent.ALARM_DISMISSED)))
        assertFalse(MedicationHistory.isExplicitResponse(log(4, 1, AlarmLogEvent.MEDICATION_TAKEN).copy(success = false)))
        assertFalse(MedicationHistory.isExplicitResponse(log(5, 1, AlarmLogEvent.MEDICATION_TAKEN).copy(category = "APP")))
    }
    @Test fun filterUsesInclusiveStartExclusiveEndAndMedication() {
        val records = listOf(log(1, 9), log(2, 10), log(3, 19), log(4, 20), log(5, 15, name = "B"))
        assertEquals(listOf(3L, 2L), MedicationHistory.filter(records, 10, 20, "A").map { it.id })
    }
    @Test fun technicalEventsAndErrorsStayOutOfPatientHistory() {
        assertFalse(MedicationHistory.isVisible(log(1, 0, AlarmLogEvent.ALARM_SCHEDULED)))
        assertFalse(MedicationHistory.isVisible(log(2, 0).copy(category = "APP")))
        assertFalse(MedicationHistory.isVisible(log(3, 0, "UNKNOWN_EVENT")))
        assertTrue(MedicationHistory.isVisible(log(4, 0, AlarmLogEvent.ALARM_FAILED)))
    }
    @Test fun allDatesKeepsOldEventsAndSortsTiesById() {
        assertEquals(listOf(3L, 2L, 1L), MedicationHistory.filter(listOf(log(1, 1), log(2, 2), log(3, 2)), null, null, null).map { it.id })
    }
    @Test fun utcPickerDayBecomesLocalCalendarDay() {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(2026, Calendar.OCTOBER, 2) }.timeInMillis
        val zone = TimeZone.getTimeZone("America/Mexico_City")
        val result = Calendar.getInstance(zone).apply { timeInMillis = MedicationHistory.pickerDay(utc, zone = zone) }
        assertEquals(2, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, result.get(Calendar.HOUR_OF_DAY))
    }
    @Test fun dayEndHandlesDaylightSavingInsteadOfAdding24Hours() {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(2026, Calendar.MARCH, 8) }.timeInMillis
        val zone = TimeZone.getTimeZone("America/New_York")
        assertEquals(23L * 3600000L, MedicationHistory.pickerDay(utc, true, zone) - MedicationHistory.pickerDay(utc, false, zone))
    }
}
