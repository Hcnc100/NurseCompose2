package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderScheduleTest {
    @Test
    fun `returns the only occurrence within a single-day range`() {
        val reminder = reminder(startAt = 10_000, endAt = 10_000, intervalHours = 1)

        assertEquals(listOf(10_000L), ReminderSchedule.occurrencesBetween(reminder, 0, 20_000))
    }

    @Test
    fun `returns repeated occurrences without an end date`() {
        val reminder = reminder(startAt = 0, endAt = null, intervalHours = 2)

        assertEquals(
            listOf(7_200_000L, 14_400_000L),
            ReminderSchedule.occurrencesBetween(reminder, 1, 15_000_000),
        )
    }

    @Test
    fun `does not return occurrences after a date range ends`() {
        val reminder = reminder(startAt = 0, endAt = 7_200_000, intervalHours = 1)

        assertEquals(
            listOf(0L, 3_600_000L, 7_200_000L),
            ReminderSchedule.occurrencesBetween(reminder, 0, 20_000_000),
        )
    }

    private fun reminder(startAt: Long, endAt: Long?, intervalHours: Int) = MedicationReminderEntity(
        name = "Medication",
        startAt = startAt,
        endAt = endAt,
        intervalHours = intervalHours,
    )
}