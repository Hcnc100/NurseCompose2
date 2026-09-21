package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationReminderCollisionDetectorTest {
    @Test
    fun `detects occurrences in the same minute`() {
        val collisions = MedicationReminderCollisionDetector.findCollisions(
            candidate = reminder(id = 1, name = "Aspirin", startAt = 1_000),
            existingReminders = listOf(reminder(id = 2, name = "Vitamin", startAt = 30_000)),
            windowStart = 0,
            windowEnd = 60_000,
        )

        assertEquals(listOf("Vitamin"), collisions.map { it.reminder.name })
        assertEquals(listOf(0L), collisions.map { it.occurrenceMinute })
    }

    @Test
    fun `ignores inactive reminders and the reminder being edited`() {
        val candidate = reminder(id = 1, name = "Aspirin", startAt = 0)

        val collisions = MedicationReminderCollisionDetector.findCollisions(
            candidate = candidate,
            existingReminders = listOf(
                candidate,
                reminder(id = 2, name = "Inactive", startAt = 0, isActive = false),
            ),
            windowStart = 0,
            windowEnd = 60_000,
        )

        assertEquals(emptyList<MedicationReminderCollision>(), collisions)
    }

    @Test
    fun `does not report occurrences outside the configured date range`() {
        val collisions = MedicationReminderCollisionDetector.findCollisions(
            candidate = reminder(id = 1, name = "Aspirin", startAt = 0, intervalHours = 1),
            existingReminders = listOf(
                reminder(id = 2, name = "Limited", startAt = 0, endAt = 0, intervalHours = 1),
            ),
            windowStart = 3_600_000,
            windowEnd = 7_200_000,
        )

        assertEquals(emptyList<MedicationReminderCollision>(), collisions)
    }

    private fun reminder(
        id: Long,
        name: String,
        startAt: Long,
        endAt: Long? = null,
        intervalHours: Int = 1,
        isActive: Boolean = true,
    ) = MedicationReminderEntity(
        id = id,
        name = name,
        startAt = startAt,
        endAt = endAt,
        intervalHours = intervalHours,
        isActive = isActive,
    )
}