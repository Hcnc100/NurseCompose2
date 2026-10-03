package com.nullpointer.nourseCompose.domain.medication

import org.junit.Assert.*
import org.junit.Test

class ReminderIntervalUnitTest {
    @Test fun convertsHoursAndLocaleDecimals() {
        assertEquals(480, ReminderIntervalUnit.HOURS.toMinutes("8"))
        assertEquals(90, ReminderIntervalUnit.HOURS.toMinutes("1.5"))
        assertEquals(90, ReminderIntervalUnit.HOURS.toMinutes("1,5"))
        assertEquals(15, ReminderIntervalUnit.MINUTES.toMinutes("15"))
    }
    @Test fun switchingUnitsPreservesWholeMinutes() {
        for (minutes in listOf(1,17,59,60,90,480,1440,Int.MAX_VALUE)) {
            assertEquals(minutes, ReminderIntervalUnit.HOURS.toMinutes(ReminderIntervalUnit.HOURS.format(minutes)))
        }
        assertEquals(ReminderIntervalUnit.HOURS, ReminderIntervalUnit.forMinutes(480))
        assertEquals(ReminderIntervalUnit.MINUTES, ReminderIntervalUnit.forMinutes(90))
    }
    @Test fun rejectsInvalidAndOverflowingValues() {
        for (text in listOf("", "0", "-1", "abc", "1..5", "999999999999")) {
            assertNull(ReminderIntervalUnit.HOURS.toMinutes(text))
            assertNull(ReminderIntervalUnit.MINUTES.toMinutes(text))
        }
        assertNull(ReminderIntervalUnit.MINUTES.toMinutes("1.5"))
        assertNull(ReminderIntervalUnit.HOURS.toMinutes("0.001"))
    }
}
