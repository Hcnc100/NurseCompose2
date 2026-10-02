package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderDurationTest {
    @Test fun newOrChangedSingleDoseRequiresFutureTimeButLegacyIsPreserved() {
        org.junit.Assert.assertTrue(ReminderDuration.requiresFutureTime(100, null, null, 100))
        org.junit.Assert.assertTrue(ReminderDuration.requiresFutureTime(99, null, null, 100))
        org.junit.Assert.assertFalse(ReminderDuration.requiresFutureTime(101, null, null, 100))
        org.junit.Assert.assertFalse(ReminderDuration.requiresFutureTime(90, 90, 90, 100))
        org.junit.Assert.assertTrue(ReminderDuration.requiresFutureTime(90, 90, null, 100))
        org.junit.Assert.assertTrue(ReminderDuration.requiresFutureTime(90, 89, 89, 100))
    }
    private fun at(value: String, zone: ZoneId) = LocalDateTime.parse(value).atZone(zone).toInstant().toEpochMilli()

    @Test fun repeatsOnlyWithinFirstLocalDay() {
        val zone = ZoneId.of("America/Mexico_City")
        val start = at("2026-10-02T09:00:00", zone)
        val end = ReminderDuration.endOfStartDay(start, zone)
        val reminder = MedicationReminderEntity(name = "Test", startAt = start, endAt = end, intervalHours = 1)
        val times = ReminderSchedule.occurrencesBetween(reminder, start, at("2026-10-03T12:00:00", zone))
        assertEquals(15, times.size)
        assertEquals(at("2026-10-02T23:00:00", zone), times.last())
        assertEquals(at("2026-10-03T00:00:00", zone) - 1, end)
    }

    @Test fun calendarBoundaryRespectsBothDaylightSavingTransitions() {
        val zone = ZoneId.of("America/New_York")
        for ((date, hours) in listOf("2026-03-08" to 23L, "2026-11-01" to 25L)) {
            val start = at("${date}T00:00:00", zone)
            assertEquals(hours * 3_600_000L, ReminderDuration.endOfStartDay(start, zone) - start + 1)
        }
    }

    @Test fun lateStartDoesNotBecomeTwentyFourHoursOfDoses() {
        val zone = ZoneId.of("Asia/Tokyo")
        val start = at("2026-10-02T23:45:00", zone)
        assertEquals(at("2026-10-03T00:00:00", zone) - 1, ReminderDuration.endOfStartDay(start, zone))
    }

    @Test fun legacyEqualStartAndEndRemainsASingleOccurrence() {
        val reminder = MedicationReminderEntity(name = "Legacy", startAt = 10_000, endAt = 10_000, intervalHours = 1)
        assertEquals(listOf(10_000L), ReminderSchedule.occurrencesBetween(reminder, 0, 100_000))
    }
}
