package com.nullpointer.nourseCompose.ui.screens.medication

import androidx.compose.runtime.Composable
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

/** Dedicated HomeGraph destination so the editor is a peer screen, not an embedded dialog. */
@Destination
@com.ramcosta.composedestinations.annotation.RootNavGraph
@Composable
fun MedicationReminderEditorScreen(
    destinationsNavigator: DestinationsNavigator,
    reminderId: Long? = null,
    viewModel: MedicationReminderViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    val reminder = reminders.firstOrNull { it.id == reminderId }
    var pendingReminder by remember { mutableStateOf<MedicationReminderEntity?>(null) }
    MedicationReminderEditor(
        reminder = reminder,
        onDismiss = { destinationsNavigator.navigateUp() },
        onSave = { reminder ->
            val collisions = MedicationReminderCollisionDetector.findCollisions(reminder, reminders, System.currentTimeMillis(), System.currentTimeMillis() + 48 * 60 * 60 * 1_000L)
            if (collisions.isEmpty()) {
                viewModel.save(reminder)
                destinationsNavigator.navigateUp()
            } else pendingReminder = reminder
        }
    )
    pendingReminder?.let { candidate ->
        val collisions = MedicationReminderCollisionDetector.findCollisions(candidate, reminders, System.currentTimeMillis(), System.currentTimeMillis() + 48 * 60 * 60 * 1_000L)
        AlertDialog(
            onDismissRequest = { pendingReminder = null },
            title = { Text(stringResource(R.string.title_medication_collision)) },
            text = { Text(stringResource(R.string.message_medication_collision, collisions.joinToString { it.reminder.name })) },
            confirmButton = { TextButton(onClick = { viewModel.save(candidate); destinationsNavigator.navigateUp() }) { Text(stringResource(R.string.action_save_anyway)) } },
            dismissButton = { TextButton(onClick = { pendingReminder = null }) { Text(stringResource(R.string.button_cancel_title)) } }
        )
    }
}
