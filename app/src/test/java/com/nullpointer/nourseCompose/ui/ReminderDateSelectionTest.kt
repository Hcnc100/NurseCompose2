package com.nullpointer.nourseCompose.ui

import com.nullpointer.nourseCompose.ui.screens.medication.ReminderDateSelection
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderDateSelectionTest {
    @Test fun preservesLocalDayAcrossBothSidesOfUtc() {
        for (zoneName in listOf("America/Mexico_City", "Asia/Tokyo", "Pacific/Kiritimati")) {
            val zone = TimeZone.getTimeZone(zoneName)
            for (hour in listOf(0, 23)) {
                val original = Calendar.getInstance(zone).apply {
                    clear()
                    set(2026, Calendar.OCTOBER, 2, hour, 45)
                }
                val selected = ReminderDateSelection.pickerDate(original.timeInMillis, zone)
                val result = Calendar.getInstance(zone).apply {
                    timeInMillis = ReminderDateSelection.localDateTime(selected, hour, 45, zone)
                }
                assertEquals(original.timeInMillis, result.timeInMillis)
                assertEquals(2, result.get(Calendar.DAY_OF_MONTH))
            }
        }
    }
}
