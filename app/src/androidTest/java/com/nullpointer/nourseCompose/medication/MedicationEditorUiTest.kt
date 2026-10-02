package com.nullpointer.nourseCompose.medication

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.screens.medication.MedicationReminderEditor
import com.nullpointer.nourseCompose.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MedicationEditorUiTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)

    @Test fun newSingleDoseWithElapsedTimeShowsErrorInsteadOfSaving() {
        var saved = 0
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(null, {}, { saved++ }) } }
        compose.onNodeWithText(text(R.string.label_medication_name)).performTextInput("New single dose")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText(text(R.string.schedule_single_dose)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.action_save)).performClick()
        compose.onNodeWithText(text(R.string.error_single_dose_time)).assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, saved) }
    }

    @Test fun singleDoseHidesRecurrenceWithoutLosingDraftInterval() {
        val start = 1_900_000_000_000L
        val reminder = com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity(
            name = "Conditional draft", startAt = start, intervalHours = 1)
        var saved: com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity? = null
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(reminder, {}, { saved = it }) } }
        compose.onNodeWithText(text(R.string.label_interval_hours)).performScrollTo().performTextReplacement("0")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText(text(R.string.schedule_single_dose)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.label_interval_hours)).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.label_next_doses)).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.label_end_date), substring = true).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.action_save)).performClick()
        compose.runOnIdle { assertEquals(start, saved?.endAt); assertEquals(60, saved?.intervalMinutes) }
        compose.onNodeWithText(text(R.string.schedule_indefinite)).performScrollTo().performClick()
        compose.onNodeWithText("0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.action_save)).performClick()
        compose.onNodeWithText(text(R.string.error_medication_interval)).performScrollTo().assertIsDisplayed()
    }

    @Test fun finalDateAppearsOnlyForDateRange() {
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(null, {}, {}) } }
        compose.onNodeWithText(text(R.string.label_end_date), substring = true).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.schedule_date_range)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.label_end_date), substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.schedule_one_day)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.label_end_date), substring = true).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.description_schedule_one_day)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.schedule_single_dose)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.description_schedule_one_day)).assertDoesNotExist()
    }

    @Test fun oneDaySelectionSavesCalendarDayEnd() {
        val start = 1_900_000_000_000L
        val reminder = com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity(
            id = 17, name = "Duration test", startAt = start, intervalHours = 1)
        var saved: com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity? = null
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(reminder, {}, { saved = it }) } }
        compose.onNodeWithText(text(R.string.schedule_one_day)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.description_schedule_one_day)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.action_save)).performClick()
        compose.runOnIdle {
            assertEquals(com.nullpointer.nourseCompose.domain.medication.ReminderDuration.endOfStartDay(start), saved?.endAt)
            assertEquals(17L, saved?.id)
        }
    }

    @Test fun savingLegacySingleDoseDoesNotExtendItsSchedule() {
        val start = 1_900_000_000_000L
        val reminder = com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity(
            id = 18, name = "Legacy dose", startAt = start, endAt = start, intervalHours = 1)
        var saved: com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity? = null
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(reminder, {}, { saved = it }) } }
        compose.onNodeWithText(text(R.string.schedule_single_dose)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.action_save)).performClick()
        compose.runOnIdle { assertEquals(start, saved?.endAt) }
    }

    @Test fun keyboardNextMovesThroughMedicationAndDosageWithoutSaving() {
        var saved = 0
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(null, {}, { saved++ }) } }
        compose.onNodeWithText(text(R.string.label_medication_name))
            .performClick().performTextInput("Keyboard draft")
        compose.onNodeWithText("Keyboard draft").performImeAction()
        compose.onNodeWithText(text(R.string.label_dosage_optional)).assertIsFocused()
            .performTextInput("One tablet")
        compose.onNodeWithText("One tablet").performImeAction()
        compose.onNodeWithText(text(R.string.label_comment_optional)).assertIsFocused()
        compose.runOnIdle { assertEquals(0, saved) }
    }

    @Test fun toolbarBackConfirmsDirtyDraftAndKeepEditingPreservesIt() {
        var dismissed = 0
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(null, { dismissed++ }, {}) } }
        compose.onNodeWithText(text(R.string.label_medication_name)).performTextInput("UI test medicine")
        compose.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        compose.onNodeWithText(text(R.string.reminder_discard_title)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.reminder_keep_editing)).performClick()
        compose.onNodeWithText("UI test medicine").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, dismissed) }
        compose.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        compose.onNodeWithText(text(R.string.reminder_discard_action)).performClick()
        compose.runOnIdle { assertEquals(1, dismissed) }
    }

    @Test fun systemBackConfirmsDirtyDraft() {
        var dismissed = 0
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(null, { dismissed++ }, {}) } }
        compose.onNodeWithText(text(R.string.label_medication_name)).performTextInput("Unsaved")
        Espresso.closeSoftKeyboard()
        Espresso.pressBack()
        compose.onNodeWithText(text(R.string.reminder_discard_title)).assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, dismissed) }
    }

    @Test fun cleanDraftLeavesWithoutConfirmation() {
        var dismissed = 0
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(null, { dismissed++ }, {}) } }
        compose.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        compose.runOnIdle { assertEquals(1, dismissed) }
        compose.onNodeWithText(text(R.string.reminder_discard_title)).assertDoesNotExist()
    }

    @Test fun savingCannotBeDismissedBySystemBack() {
        var dismissed = 0
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(null, { dismissed++ }, {}, isSaving = true) } }
        Espresso.pressBack()
        compose.onNode(isDialog()).assert(hasAnyDescendant(hasText(text(R.string.reminder_saving)))).assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, dismissed) }
    }

    @Test fun failureRemainsVisibleInEditor() {
        compose.setContent { MyApplicationTheme { MedicationReminderEditor(null, {}, {}, saveError = true) } }
        compose.onNodeWithText(text(R.string.reminder_save_failed)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.action_save)).assertIsEnabled()
    }

    @Test fun restoredDraftStillConfirmsUnsavedChanges() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { MyApplicationTheme { MedicationReminderEditor(null, {}, {}) } }
        compose.onNodeWithText(text(R.string.label_medication_name)).performTextInput("Restored draft")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Restored draft").assertIsDisplayed()
        compose.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        compose.onNodeWithText(text(R.string.reminder_discard_title)).assertIsDisplayed()
    }

    @Test fun unregisteredScheduleIsLabeledPlannedNotConfirmed() {
        val registered = androidx.compose.runtime.mutableStateOf<Long?>(86_400_000L)
        compose.setContent {
            MyApplicationTheme {
                com.nullpointer.nourseCompose.ui.screens.medication.MedicationReminderCard(
                    com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity(
                        name = "Test", startAt = 86_400_000L, intervalHours = 1),
                    now = 0, scheduledAt = registered.value, onClick = {}, onActiveChange = {}, onDelete = {},
                )
            }
        }
        compose.onNodeWithText(text(R.string.label_next_reminder_alarm)).assertIsDisplayed()
        compose.runOnIdle { registered.value = null }
        compose.onNodeWithText(text(R.string.label_next_reminder_dose)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.label_next_reminder_alarm)).assertDoesNotExist()
    }
}
