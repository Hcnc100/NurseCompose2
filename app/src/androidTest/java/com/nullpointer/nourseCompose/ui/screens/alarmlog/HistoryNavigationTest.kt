package com.nullpointer.nourseCompose.ui.screens.alarmlog

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import java.io.File
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import com.nullpointer.nourseCompose.ui.screens.home.actions.DrawerActions
import com.nullpointer.nourseCompose.ui.screens.home.widgets.DrawerContent
import com.nullpointer.nourseCompose.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Parameterized::class)
class HistoryNavigationTest(private val language: String, private val dark: Boolean, private val fontScale: Float) {
    @get:Rule val compose = createComposeRule()
    private val base = InstrumentationRegistry.getInstrumentation().targetContext
    private val config = Configuration(base.resources.configuration).apply { setLocale(Locale(language)) }
    private val context = base.createConfigurationContext(config)
    private fun content(block: @androidx.compose.runtime.Composable () -> Unit) {
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides config,
                LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MyApplicationTheme(darkTheme = dark) { Surface(Modifier.fillMaxSize()) { block() } }
            }
        }
    }
    @Test fun drawerHasOnlyEverydayDestinations() {
        var clicked: DrawerActions? = null
        content { Box(Modifier.fillMaxSize()) { Box(Modifier.width(320.dp)) { DrawerContent { clicked = it } } } }
        for (id in listOf(R.string.title_medication_history, R.string.title_reports, R.string.title_settings_option)) {
            compose.onNodeWithText(context.getString(id)).assertIsDisplayed()
        }
        compose.onNodeWithText(context.getString(R.string.title_clear_all_data_option)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.title_option_import)).assertDoesNotExist()
        screenshot("drawer")
        compose.onNodeWithText(context.getString(R.string.title_medication_history)).performClick()
        assertEquals(DrawerActions.ALARM_LOGS, clicked)
    }
    @Test fun patientHistorySeparatesDismissalAndExplicitConfirmation() {
        val now = System.currentTimeMillis()
        val logs = listOf(
            AlarmLogEntity(id = 1, reminderName = "Medicamento de prueba con nombre largo", eventType = AlarmLogEvent.MEDICATION_TAKEN, occurredAt = now, success = true),
            AlarmLogEntity(id = 2, reminderName = "Otra medicación", eventType = AlarmLogEvent.ALARM_DISMISSED, occurredAt = now - 1000, success = true),
            AlarmLogEntity(id = 3, reminderName = "Técnico", eventType = AlarmLogEvent.ALARM_SCHEDULED, occurredAt = now - 2000, success = true, details = "scheduleType=ALARM_CLOCK"),
            AlarmLogEntity(id = 4, reminderName = "No tomada explícita", eventType = AlarmLogEvent.MEDICATION_NOT_TAKEN, occurredAt = now - 3000, success = true),
        )
        content { AlarmHistoryContent(logs, technical = false, onBack = {}) }
        compose.onNodeWithText(context.getString(R.string.history_custom)).performClick()
        try {
            // Dialog windows attach asynchronously; a semantics node may exist before the window is visible.
            compose.waitUntil(10_000) {
                runCatching { compose.onNodeWithText(context.getString(R.string.history_apply)).assertIsDisplayed() }.isSuccess
            }
        } finally { screenshot("date-range", dialog = true) }
        compose.onNodeWithText(context.getString(R.string.history_apply)).assertIsDisplayed().assertIsNotEnabled()
        // Material calendar exposes full localized dates, not the visually rendered day number.
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        val fullDate = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL, Locale(language))
        repeat(2) {
            compose.onNode(hasClickAction() and hasText(fullDate.format(calendar.time), substring = true))
                .assertIsDisplayed().performClick()
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        compose.onNodeWithText(context.getString(R.string.history_apply)).assertIsEnabled().performClick()
        compose.onNodeWithText(context.getString(R.string.history_custom)).performClick()
        compose.waitUntil(10_000) {
            runCatching { compose.onNodeWithText(context.getString(R.string.message_cancel_dialog)).assertIsDisplayed() }.isSuccess
        }
        compose.onNodeWithText(context.getString(R.string.message_cancel_dialog)).performClick()
        compose.onNodeWithText(context.getString(R.string.history_all)).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(context.getString(R.string.history_event_taken)))
        compose.onNodeWithText(context.getString(R.string.history_event_taken)).assertIsDisplayed()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(context.getString(R.string.history_event_dismissed)))
        compose.onNodeWithText(context.getString(R.string.history_event_dismissed)).assertIsDisplayed()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(context.getString(R.string.history_event_not_taken)))
        compose.onNodeWithText(context.getString(R.string.history_event_not_taken)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.history_not_taken_note)).assertExists()
        compose.onNodeWithText("scheduleType=ALARM_CLOCK").assertDoesNotExist()
        compose.onNode(hasScrollAction()).performScrollToIndex(0)
        screenshot("history")
    }
    @Test fun emptyFiltersRecoverOldHistoryAndMedicationSelectionWorks() {
        val old = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val logs = listOf(
            AlarmLogEntity(id = 1, reminderName = "Medicamento A", eventType = AlarmLogEvent.MEDICATION_TAKEN, occurredAt = old, success = true),
            AlarmLogEntity(id = 2, reminderName = "Medicamento B", eventType = AlarmLogEvent.ALARM_DISMISSED, occurredAt = old, success = true),
            AlarmLogEntity(id = 3, reminderName = "Solo técnico", eventType = AlarmLogEvent.ALARM_SCHEDULED, occurredAt = old, success = true),
        )
        content { AlarmHistoryContent(logs, technical = false, onBack = {}) }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(context.getString(R.string.history_clear_filters)))
        compose.onNodeWithText(context.getString(R.string.history_clear_filters)).assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.history_event_taken)).assertExists()
        compose.onNodeWithText(context.getString(R.string.history_event_dismissed)).assertExists()
        compose.onNode(hasScrollAction()).performScrollToIndex(0)
        compose.onNodeWithText(context.getString(R.string.history_all_medications)).performClick()
        compose.onNodeWithText("Solo técnico").assertDoesNotExist()
        compose.onAllNodesWithText("Medicamento A").onLast().performClick()
        compose.onNodeWithText(context.getString(R.string.history_event_taken)).assertExists()
        compose.onNodeWithText(context.getString(R.string.history_event_dismissed)).assertDoesNotExist()
        screenshot("filtered-history")
    }
    @Test fun correctionNeedsConfirmationAndCanBeReversed() {
        var record by androidx.compose.runtime.mutableStateOf(AlarmLogEntity(id = 101, reminderName = "Medicamento de prueba",
            eventType = AlarmLogEvent.MEDICATION_TAKEN, occurredAt = System.currentTimeMillis(), success = true))
        var corrections = 0
        content {
            AlarmHistoryContent(listOf(record), false, onBack = {}, onCorrection = { id, expected, replacement ->
                assertEquals(record.id, id)
                assertEquals(record.eventType, expected)
                corrections++
                record = record.copy(eventType = replacement)
            })
        }
        fun openCorrection() {
            compose.onNode(hasScrollAction()).performScrollToNode(hasText(context.getString(R.string.history_correct_response)))
            compose.onNodeWithText(context.getString(R.string.history_correct_response)).performClick()
            compose.waitUntil(10_000) {
                runCatching { compose.onNodeWithText(context.getString(R.string.history_save_correction)).assertIsDisplayed() }.isSuccess
            }
        }
        openCorrection()
        screenshot("correction-dialog", dialog = true)
        compose.onNodeWithText(context.getString(R.string.message_cancel_dialog)).performClick()
        assertEquals(0, corrections)
        openCorrection()
        compose.onNodeWithText(context.getString(R.string.history_save_correction)).assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.history_event_not_taken)).assertExists()
        assertEquals(1, corrections)
        openCorrection()
        compose.onNodeWithText(context.getString(R.string.history_save_correction)).performClick()
        compose.onNodeWithText(context.getString(R.string.history_event_taken)).assertExists()
        assertEquals(2, corrections)
    }

    private fun screenshot(name: String, dialog: Boolean = false) {
        val directory = File(base.getExternalFilesDir(null), "qa-navigation").apply { mkdirs() }
        if (dialog) InstrumentationRegistry.getInstrumentation().uiAutomation.waitForIdle(500, 5_000)
        val bitmap = if (dialog) InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            else compose.onRoot().captureToImage().asAndroidBitmap()
        File(directory, "$name-$language-dark-$dark-font-$fontScale.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}-dark={1}-font={2}")
        fun parameters() = listOf("en", "es").flatMap { language -> listOf(false, true).flatMap { dark ->
            listOf(1f, 2f).map { arrayOf<Any>(language, dark, it) }
        } }
    }
}
