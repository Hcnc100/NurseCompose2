package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity

/** Prefer the actual scheduler time, including snoozes, over the regular cadence. */
object ReminderNextAlarm {
    fun at(reminder: MedicationReminderEntity, now: Long, scheduledAt: Long?): Long? {
        if (!reminder.isActive || reminder.intervalMinutes <= 0) return null
        scheduledAt?.takeIf { it >= now }?.let { return it }
        return ReminderSchedule.occurrencesBetween(
            reminder, now, maxOf(now, reminder.startAt) + reminder.intervalMinutes * 60_000L,
        ).firstOrNull()
    }
}
