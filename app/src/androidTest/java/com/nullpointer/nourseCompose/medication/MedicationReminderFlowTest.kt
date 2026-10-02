package com.nullpointer.nourseCompose.medication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.nullpointer.nourseCompose.MainActivity
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.datasource.medication.local.MedicationReminderLocalDataSourceImpl
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepoImpl
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepoImpl
import com.nullpointer.nourseCompose.inject.database.MeasureDatabaseModule
import com.nullpointer.nourseCompose.notifications.MedicationReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Opt-in UI + real database flow, only on explicitly selected disposable QA emulators. */
class MedicationReminderFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefix = "__QA_FLOW_${System.nanoTime()}__"
    private val database by lazy { MeasureDatabaseModule.provideNurseDatabase(context, MeasureDatabaseModule.provideNameDatabase()) }
    private val reminders by lazy { MedicationReminderRepoImpl(MedicationReminderLocalDataSourceImpl(database.getMedicationReminderDao())) }
    private val scheduler by lazy { MedicationReminderScheduler(context, AlarmLogRepoImpl(database.getAlarmLogDao())) }
    private var enabled = false
    private fun text(id: Int) = context.getString(id)
    private fun records() = runBlocking { reminders.observeAll().first().filter { it.name.startsWith(prefix) } }

    @Before fun prepare() {
        assumeTrue("Explicit disposable-emulator opt-in required",
            InstrumentationRegistry.getArguments().getString("reminderFlowSuite") == "true")
        enabled = true
        compose.waitForIdle()
        if (compose.onAllNodesWithText(text(R.string.intro_skip)).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText(text(R.string.intro_skip)).performClick()
        }
        compose.waitUntil(10_000) { compose.activity.hasWindowFocus() }
    }

    @After fun cleanup() {
        if (!enabled) return
        records().forEach { reminder -> scheduler.cancel(reminder.id); runBlocking { reminders.delete(reminder) } }
        database.openHelper.writableDatabase.execSQL("DELETE FROM alarm_logs WHERE reminderName IN (?, ?)",
            arrayOf("$prefix original", "$prefix edited"))
        database.close()
    }

    @Test fun createEditAndDeleteUsesSamePersistedReminder() {
        compose.onNodeWithText(text(R.string.title_medications)).performClick()
        compose.onNodeWithContentDescription(text(R.string.action_add_medication)).performClick()
        compose.onNodeWithText(text(R.string.label_medication_name)).performTextInput("$prefix original")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText(text(R.string.action_save)).performClick()
        compose.waitUntil(15_000) { records().size == 1 }
        val original = records().single()
        compose.waitUntil(15_000) { compose.onAllNodesWithText(original.name).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(original.name).performClick()
        compose.onNodeWithText(original.name).performTextReplacement("$prefix edited")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText(text(R.string.action_save)).performClick()
        compose.waitUntil(15_000) { records().singleOrNull()?.name == "$prefix edited" }
        assertEquals("Editing must keep the persisted ID", original.id, records().single().id)
        compose.waitUntil(15_000) { compose.onAllNodesWithText("$prefix edited").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription(text(R.string.action_delete)).performClick()
        compose.onNodeWithText(text(R.string.title_delete_reminder)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.action_delete)).performClick()
        compose.waitUntil(15_000) { records().isEmpty() }
        compose.onNodeWithText("$prefix edited").assertDoesNotExist()
        assertNull("Deleting must remove the registered alarm", scheduler.nextAlarmTimes.value[original.id])
    }
}
