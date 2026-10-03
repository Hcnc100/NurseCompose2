package com.nullpointer.nourseCompose.ui.screens.alarmlog

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import com.nullpointer.nourseCompose.MainActivity
import com.nullpointer.nourseCompose.R
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Real navigation only: no import, export, deletion or medication changes. */
class HistoryNavigationFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun text(id: Int) = compose.activity.getString(id)
    @Test fun drawerSettingsTechnicalLogAndReportsRemainReachable() {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(text(R.string.intro_skip)).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithContentDescription(text(R.string.action_open_menu)).fetchSemanticsNodes().isNotEmpty()
        }
        if (compose.onAllNodesWithText(text(R.string.intro_skip)).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText(text(R.string.intro_skip)).performClick()
        }
        compose.onNodeWithContentDescription(text(R.string.action_open_menu)).performClick()
        snapshot("actual-drawer")
        compose.onNodeWithText(text(R.string.title_reports)).performClick()
        compose.onNodeWithText(text(R.string.title_reports)).assertIsDisplayed()
        compose.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        compose.onNodeWithContentDescription(text(R.string.action_open_menu)).performClick()
        compose.onNodeWithText(text(R.string.title_settings_option)).performClick()
        compose.onNodeWithText(text(R.string.settings_data_files)).assertIsDisplayed()
        snapshot("actual-settings")
        compose.onNodeWithText(text(R.string.title_technical_alarm_log)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.title_technical_alarm_log)).assertIsDisplayed()
        compose.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        compose.onNodeWithText(text(R.string.title_diagnostics)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.title_diagnostics)).assertIsDisplayed()
        compose.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        compose.onNodeWithText(text(R.string.settings_delete_measurements)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.settings_delete_scope)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.message_cancel_dialog)).performClick()
        compose.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        compose.onNodeWithContentDescription(text(R.string.action_open_menu)).performClick()
        compose.onNodeWithText(text(R.string.title_medication_history)).performClick()
        compose.onNodeWithText(text(R.string.title_medication_history)).assertIsDisplayed()
    }
    private fun snapshot(name: String) {
        val directory = File(compose.activity.getExternalFilesDir(null), "qa-navigation").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(directory, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }
}
