package com.nullpointer.nourseCompose.ui.screens.medication

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderCollisionDetector
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.annotation.RootNavGraph

/** Root destination: a sibling of Home, outside its drawer and bottom-navigation Scaffold. */
@Destination
@RootNavGraph
@Composable
fun MedicationReminderEditorScreen(
    destinationsNavigator: DestinationsNavigator,
    reminderId: Long? = null,
    viewModel: MedicationReminderViewModel = hiltViewModel()
) {
    val reminderData by viewModel.reminderData.collectAsState()
    val reminders = reminderData.orEmpty()
    val saveStatus by viewModel.saveStatus.collectAsState()
    androidx.compose.runtime.LaunchedEffect(saveStatus) {
        if (saveStatus == MedicationReminderViewModel.SaveStatus.SAVED) destinationsNavigator.navigateUp()
    }
    val reminder = reminders.firstOrNull { it.id == reminderId }
    var pendingReminder by remember { mutableStateOf<MedicationReminderEntity?>(null) }
    if (reminderData == null || (reminderId != null && reminder == null)) {
        androidx.compose.material3.Scaffold(topBar = {
            com.nullpointer.nourseCompose.ui.share.AppTopBar(
                stringResource(if (reminderId == null) R.string.title_add_medication else R.string.title_edit_medication),
                onBack = { destinationsNavigator.navigateUp() },
            )
        }) { padding ->
            androidx.compose.foundation.layout.Box(
                androidx.compose.ui.Modifier.padding(padding).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                if (reminderData == null) androidx.compose.material3.CircularProgressIndicator()
                else Text(stringResource(R.string.reminder_not_found))
            }
        }
        return
    }
    MedicationReminderEditor(
        reminder = reminder,
        isSaving = saveStatus == MedicationReminderViewModel.SaveStatus.SAVING,
        saveError = saveStatus == MedicationReminderViewModel.SaveStatus.ERROR,
        onDismiss = { destinationsNavigator.navigateUp() },
        onSave = { reminder ->
            val collisions = MedicationReminderCollisionDetector.findCollisions(reminder, reminders, System.currentTimeMillis(), System.currentTimeMillis() + 48 * 60 * 60 * 1_000L)
            if (collisions.isEmpty()) {
                viewModel.save(reminder)
            } else pendingReminder = reminder
        }
    )
    pendingReminder?.let { candidate ->
        val collisions = MedicationReminderCollisionDetector.findCollisions(candidate, reminders, System.currentTimeMillis(), System.currentTimeMillis() + 48 * 60 * 60 * 1_000L)
        AlertDialog(
            onDismissRequest = { pendingReminder = null },
            title = { Text(stringResource(R.string.title_medication_collision)) },
            text = { Text(stringResource(R.string.message_medication_collision, collisions.joinToString { it.reminder.name })) },
            confirmButton = { TextButton(onClick = { pendingReminder = null; viewModel.save(candidate) }) { Text(stringResource(R.string.action_save_anyway)) } },
            dismissButton = { TextButton(onClick = { pendingReminder = null }) { Text(stringResource(R.string.button_cancel_title)) } }
        )
    }
}
