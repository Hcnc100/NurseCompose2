package com.nullpointer.nourseCompose.domain.alarm

import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import java.util.Calendar
import java.util.TimeZone

/** User-facing events only. Old dismissals cannot safely be backfilled as intake. */
object MedicationHistory {
    private val visibleEvents = setOf(AlarmLogEvent.ALARM_LAUNCHED, AlarmLogEvent.ALARM_DISMISSED,
        AlarmLogEvent.ALARM_SNOOZED, AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN, AlarmLogEvent.ALARM_FAILED,
        AlarmLogEvent.ALARM_SCHEDULE_FAILED)
    fun isVisible(log: AlarmLogEntity): Boolean = log.category == "ALARM" && log.eventType in visibleEvents
    fun isExplicitResponse(log: AlarmLogEntity): Boolean = log.category == "ALARM" && log.success &&
        log.eventType in setOf(AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN)
    fun confirmsIntake(log: AlarmLogEntity): Boolean = log.eventType == AlarmLogEvent.MEDICATION_TAKEN && log.success
    fun startOfDay(time: Long, zone: TimeZone = TimeZone.getDefault()): Long = Calendar.getInstance(zone).apply {
        timeInMillis = time
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    fun daysBefore(time: Long, days: Int, zone: TimeZone = TimeZone.getDefault()): Long = Calendar.getInstance(zone).apply {
        timeInMillis = startOfDay(time, zone); add(Calendar.DAY_OF_MONTH, -days)
    }.timeInMillis
    /** Material DatePicker represents a calendar date at UTC midnight, not an actual local instant. */
    fun pickerDay(time: Long, followingDay: Boolean = false, zone: TimeZone = TimeZone.getDefault()): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = time }
        return Calendar.getInstance(zone).apply {
            clear(); set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
            if (followingDay) add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
    }
    fun filter(logs: List<AlarmLogEntity>, from: Long?, until: Long?, name: String?): List<AlarmLogEntity> =
        logs.filter { isVisible(it) && (from == null || it.occurredAt >= from) &&
            (until == null || it.occurredAt < until) && (name == null || it.reminderName == name) }
            .sortedWith(compareByDescending<AlarmLogEntity> { it.occurredAt }.thenByDescending { it.id })
}
