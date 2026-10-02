package com.nullpointer.nourseCompose.domain.medication

import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderNextAlarmTest {
    private val reminder = MedicationReminderEntity(name = "Test", startAt = 0, intervalHours = 1, intervalMinutes = 60)

    @Test fun usesSnoozedTimeInsteadOfRegularCadence() {
        assertEquals(600_000L, ReminderNextAlarm.at(reminder, 1, 600_000))
    }
    @Test fun rollsForwardAfterPreviousTime() {
        assertEquals(3_600_000L, ReminderNextAlarm.at(reminder, 1, 0))
    }
    @Test fun supportsFirstDoseFarInTheFuture() {
        assertEquals(864_000_000L, ReminderNextAlarm.at(reminder.copy(startAt = 864_000_000), 1, null))
    }
    @Test fun pausedAndFinishedHaveNoNextAlarm() {
        assertNull(ReminderNextAlarm.at(reminder.copy(isActive = false), 1, 600_000))
        assertNull(ReminderNextAlarm.at(reminder.copy(endAt = 0), 1, null))
    }
}
