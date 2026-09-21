package com.nullpointer.nourseCompose.medication

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nullpointer.nourseCompose.database.NurseDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicationRoomInstrumentedTest {
    @Test fun medicationTableContainsReminderFields() {
        val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, NurseDatabase::class.java).allowMainThreadQueries().build()
        val names = db.openHelper.writableDatabase.query("PRAGMA table_info(medication_reminders)").use { cursor -> buildSet { while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name"))) } }
        db.close()
        assertTrue(setOf("name", "startAt", "intervalHours", "isActive", "useExactAlarm").all(names::contains))
    }
}