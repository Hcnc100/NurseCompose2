package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity

/** Calculates reminder occurrences without relying on Android framework APIs. */
object ReminderSchedule {
    private const val MILLIS_PER_MINUTE = 60 * 1_000L

    fun occurrencesBetween(
        reminder: MedicationReminderEntity,
        windowStart: Long,
        windowEnd: Long,
    ): List<Long> {
        require(reminder.intervalMinutes > 0) { "intervalMinutes must be greater than zero" }
        require(windowEnd >= windowStart) { "windowEnd must not be before windowStart" }
        // A malformed persisted range should not crash the editor, collision detector,
        // or alarm receiver. It simply has no valid occurrences until corrected.
        if (reminder.endAt?.let { it < reminder.startAt } == true) return emptyList()

        val intervalMillis = reminder.intervalMinutes * MILLIS_PER_MINUTE
        val firstIndex = if (windowStart <= reminder.startAt) 0 else {
            (windowStart - reminder.startAt + intervalMillis - 1) / intervalMillis
        }
        val firstOccurrence = reminder.startAt + firstIndex * intervalMillis
        val finalOccurrence = minOf(windowEnd, reminder.endAt ?: Long.MAX_VALUE)

        if (!reminder.isActive || firstOccurrence > finalOccurrence) return emptyList()

        return buildList {
            var occurrence = firstOccurrence
            while (occurrence <= finalOccurrence) {
                add(occurrence)
                if (occurrence > Long.MAX_VALUE - intervalMillis) break
                occurrence += intervalMillis
            }
        }
    }
}
