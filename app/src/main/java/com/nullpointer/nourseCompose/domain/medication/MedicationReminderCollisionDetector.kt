package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity

/**
 * Finds schedule overlaps as a non-blocking warning for the reminder form.
 * An overlap means occurrences fall within the same clock minute.
 */
object MedicationReminderCollisionDetector {
    private const val MILLIS_PER_MINUTE = 60_000L

    fun findCollisions(
        candidate: MedicationReminderEntity,
        existingReminders: Iterable<MedicationReminderEntity>,
        windowStart: Long,
        windowEnd: Long,
    ): List<MedicationReminderCollision> {
        val candidateMinutes = ReminderSchedule
            .occurrencesBetween(candidate, windowStart, windowEnd)
            .mapTo(mutableSetOf(), ::minuteBucket)

        if (candidateMinutes.isEmpty()) return emptyList()

        return existingReminders
            .asSequence()
            .filter { it.isActive && it.id != candidate.id }
            .flatMap { existing ->
                ReminderSchedule
                    .occurrencesBetween(existing, windowStart, windowEnd)
                    .asSequence()
                    .map(::minuteBucket)
                    .filter(candidateMinutes::contains)
                    .distinct()
                    .map { minute -> MedicationReminderCollision(existing, minute) }
            }
            .sortedBy(MedicationReminderCollision::occurrenceMinute)
            .toList()
    }

    private fun minuteBucket(occurrenceAt: Long): Long = occurrenceAt / MILLIS_PER_MINUTE
}

data class MedicationReminderCollision(
    val reminder: MedicationReminderEntity,
    /** Epoch minute shared by the candidate and [reminder]. */
    val occurrenceMinute: Long,
)