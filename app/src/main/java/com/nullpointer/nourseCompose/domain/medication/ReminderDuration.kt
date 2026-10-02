package com.nullpointer.nourseCompose.domain.medication

import java.time.Instant
import java.time.ZoneId

/** Inclusive end of the first dose's calendar day, not a fixed 24-hour window. */
internal object ReminderDuration {
    fun requiresFutureTime(startAt: Long, previousStartAt: Long?, previousEndAt: Long?, now: Long): Boolean =
        startAt <= now && !(previousStartAt == startAt && previousEndAt == startAt)

    fun endOfStartDay(startAt: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(startAt).atZone(zone).toLocalDate().plusDays(1)
            .atStartOfDay(zone).toInstant().toEpochMilli() - 1
}
