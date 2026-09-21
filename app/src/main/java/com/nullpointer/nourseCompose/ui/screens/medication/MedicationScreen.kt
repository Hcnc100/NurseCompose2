package com.nullpointer.nourseCompose.ui.screens.medication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderCollision
import com.nullpointer.nourseCompose.domain.medication.ReminderSchedule
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.navigation.LocalRootNavController
import com.nullpointer.nourseCompose.navigation.graph.HomeGraph
import com.nullpointer.nourseCompose.reports.MedicationReportExporter
import com.nullpointer.nourseCompose.ui.screens.destinations.MedicationReminderEditorScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.io.File
import android.graphics.BitmapFactory

@Destination
@HomeGraph
@Composable
fun MedicationScreen(
    destinationsNavigator: DestinationsNavigator,
    viewModel: MedicationReminderViewModel = hiltViewModel()
) {
    val rootNavController = LocalRootNavController.current
    val reminders by viewModel.reminders.collectAsState()
    val context = LocalContext.current
    var pendingReminder by remember { mutableStateOf<MedicationReminderEntity?>(null) }
    var reminderToDelete by remember { mutableStateOf<MedicationReminderEntity?>(null) }
    var collisions by remember { mutableStateOf(emptyList<MedicationReminderCollision>()) }
    val reportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            uri?.let { destination ->
                context.contentResolver.openOutputStream(destination)
                    ?.use { MedicationReportExporter.write(context, reminders, it) }
            }
        }

    Scaffold(
        topBar = { },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                rootNavController.navigate(
                    MedicationReminderEditorScreenDestination.route
                )
            },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) { Icon(painterResource(R.drawable.baseline_add_24), contentDescription = stringResource(R.string.action_add_medication)) }
        },
    ) { padding ->
        if (reminders.isEmpty()) {
            EmptyMedicationState(modifier = Modifier.padding(padding).fillMaxSize())
        } else {
            Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                reminders.forEach { reminder ->
                    MedicationReminderCard(reminder = reminder, onClick = {
                        rootNavController.navigate(
                            MedicationReminderEditorScreenDestination(
                                reminderId = reminder.id
                            ).route
                        )
                    }, onActiveChange = { viewModel.setActive(reminder, it) }, onDelete = { reminderToDelete = reminder })
                }
            }
        }
    }

    pendingReminder?.takeIf { collisions.isNotEmpty() }?.let { candidate ->
        AlertDialog(
            onDismissRequest = { pendingReminder = null },
            title = { Text(stringResource(R.string.title_medication_collision)) },
            text = {
                Text(
                    stringResource(
                        R.string.message_medication_collision,
                        collisions.joinToString { it.reminder.name })
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.save(candidate); pendingReminder = null
                }) { Text(stringResource(R.string.action_save_anyway)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingReminder = null
                }) { Text(stringResource(R.string.button_calcel_title)) }
            },
        )
    }

    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text(stringResource(R.string.title_delete_reminder)) },
            text = { Text(stringResource(R.string.message_delete_reminder, reminder.name)) },
            confirmButton = { TextButton(onClick = { viewModel.delete(reminder); reminderToDelete = null }) { Text(stringResource(R.string.action_delete)) } },
            dismissButton = { TextButton(onClick = { reminderToDelete = null }) { Text(stringResource(R.string.button_cancel_title)) } }
        )
    }
}

@Composable
private fun EmptyMedicationState(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            stringResource(R.string.message_empty_medications),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            stringResource(R.string.message_empty_medications_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun MedicationReminderCard(reminder: MedicationReminderEntity, onClick: () -> Unit, onActiveChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(reminder.name, style = MaterialTheme.typography.titleLarge)
                reminder.dosage?.takeIf(String::isNotBlank)?.let { Text(it) }
                Text(stringResource(R.string.label_every_hours, reminder.intervalHours))
                Text(if (reminder.isActive) stringResource(R.string.label_active) else stringResource(R.string.label_inactive), style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(checked = reminder.isActive, onCheckedChange = onActiveChange)
                IconButton(onClick = onDelete) { Icon(painterResource(R.drawable.baseline_delete_24), contentDescription = stringResource(R.string.action_delete)) }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MedicationReminderEditor(
    reminder: MedicationReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (MedicationReminderEntity) -> Unit,
) {
    val context = LocalContext.current
    var name by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.name.orEmpty()) }
    var dosage by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.dosage.orEmpty()) }
    var comment by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.comment.orEmpty()) }
    var photoUri by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.photoUri) }
    var startAt by rememberSaveable(reminder?.id) {
        mutableStateOf(
            reminder?.startAt ?: System.currentTimeMillis()
        )
    }
    var endMode by rememberSaveable(reminder?.id) { mutableStateOf(if (reminder?.endAt == null) EndMode.INDEFINITE else if (reminder.endAt == reminder.startAt) EndMode.ONE_DAY else EndMode.RANGE) }
    var endAt by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.endAt ?: startAt) }
    var intervalText by rememberSaveable(reminder?.id) {
        mutableStateOf(
            (reminder?.intervalHours ?: 24).toString()
        )
    }
    var vibrationEnabled by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.vibrationEnabled ?: false) }
    var soundEnabled by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.soundEnabled ?: false) }
    var fullScreenAlarm by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.fullScreenAlarm ?: false) }
    var nameError by remember { mutableStateOf(false) }
    var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        if (captured) cameraUri?.let { photoUri = it.toString() }
    }
    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                photoUri = it.toString()
            }
        }
    val preview = remember(startAt, endMode, endAt, intervalText) {
        intervalText.toIntOrNull()?.takeIf { it > 0 }?.let { interval ->
            ReminderSchedule.occurrencesBetween(
                MedicationReminderEntity(
                    name = "preview",
                    startAt = startAt,
                    endAt = when (endMode) {
                        EndMode.INDEFINITE -> null; EndMode.ONE_DAY -> startAt; EndMode.RANGE -> endAt
                    },
                    intervalHours = interval
                ),
                System.currentTimeMillis(), System.currentTimeMillis() + 48 * 60 * 60 * 1_000L,
            ).take(3)
        }.orEmpty()
    }

    val saveReminder = {
        val interval = intervalText.toIntOrNull()
        if (name.isBlank() || interval == null || interval <= 0) nameError = name.isBlank()
        else onSave(
            MedicationReminderEntity(
                id = reminder?.id ?: 0,
                name = name.trim(),
                dosage = dosage.ifBlank { null },
                comment = comment.ifBlank { null },
                photoUri = photoUri,
                startAt = startAt,
                endAt = when (endMode) {
                    EndMode.INDEFINITE -> null; EndMode.ONE_DAY -> startAt; EndMode.RANGE -> endAt
                },
                intervalHours = interval,
                isActive = reminder?.isActive ?: true,
                notificationMode = reminder?.notificationMode ?: "NOTIFICATION",
                vibrationEnabled = vibrationEnabled,
                soundEnabled = soundEnabled,
                fullScreenAlarm = fullScreenAlarm
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text(stringResource(if (reminder == null) R.string.title_add_medication else R.string.title_edit_medication)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(com.nullpointer.nourseCompose.R.drawable.baseline_arrow_back_24),
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.action_save)) },
                icon = {
                    Icon(
                        painterResource(R.drawable.baseline_check_24),
                        contentDescription = null
                    )
                },
                onClick = saveReminder
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                name,
                { name = it; nameError = false },
                label = { Text(stringResource(R.string.label_medication_name)) },
                isError = nameError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError) Text(
                stringResource(R.string.error_medication_name),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
            OutlinedTextField(
                dosage,
                { dosage = it },
                label = { Text(stringResource(R.string.label_dosage_optional)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                comment,
                { comment = it },
                label = { Text(stringResource(R.string.label_comment_optional)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { showPhotoSheet = true }) { Text(stringResource(R.string.action_select_photo)) }
            photoUri?.let {
                val previewBitmap = remember(it) {
                    runCatching {
                        context.contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream)
                    }.getOrNull()
                }
                previewBitmap?.let { bitmap ->
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = stringResource(R.string.label_photo_selected), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp))
                }
                Text(
                    stringResource(R.string.label_photo_selected),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            DateTimeButton(
                stringResource(R.string.label_start_time),
                startAt,
                onChange = { startAt = it })
            OutlinedTextField(
                intervalText,
                { intervalText = it.filter(Char::isDigit) },
                label = { Text(stringResource(R.string.label_interval_hours)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.label_notification_behavior), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
                Text(stringResource(R.string.option_notification_sound), modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
                Text(stringResource(R.string.option_notification_vibration), modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = fullScreenAlarm, onCheckedChange = { fullScreenAlarm = it })
                Text(stringResource(R.string.option_full_screen_alarm), modifier = Modifier.padding(start = 8.dp))
            }
            EndModeSelector(endMode, { endMode = it })
            if (endMode == EndMode.RANGE) DateTimeButton(
                stringResource(R.string.label_end_date),
                endAt,
                onChange = { endAt = it })
            Text(
                stringResource(R.string.label_next_doses),
                style = MaterialTheme.typography.titleMedium
            )
            Text(preview.joinToString("\n") {
                DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT
                ).format(Date(it))
            }.ifBlank { stringResource(R.string.message_no_upcoming_doses) })
        }
    }

    if (showPhotoSheet) {
        ModalBottomSheet(onDismissRequest = { showPhotoSheet = false }) {
            Text(stringResource(R.string.text_select_photo_option), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            ListItem(
                headlineContent = { Text(stringResource(R.string.option_take_photo)) },
                supportingContent = { Text(stringResource(R.string.description_take_photo)) },
                leadingContent = { Icon(painterResource(R.drawable.baseline_camera_alt_24), contentDescription = null) },
                modifier = Modifier.clickable {
                    showPhotoSheet = false
                    val file = File(context.cacheDir, "images").apply { mkdirs() }.let { File(it, "medication_${System.currentTimeMillis()}.jpg") }
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.option_choose_gallery)) },
                supportingContent = { Text(stringResource(R.string.description_choose_gallery)) },
                leadingContent = { Icon(painterResource(R.drawable.baseline_photo_library_24), contentDescription = null) },
                modifier = Modifier.clickable {
                    showPhotoSheet = false
                    imagePicker.launch(arrayOf("image/*"))
                }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DateTimeButton(label: String, value: Long, onChange: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = remember(value) { Calendar.getInstance().apply { timeInMillis = value } }
    TextButton(onClick = {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(context, { _, hour, minute ->
                    onChange(
                        Calendar.getInstance().apply {
                            set(year, month, day, hour, minute, 0); set(
                            Calendar.MILLISECOND,
                            0
                        )
                        }.timeInMillis
                    )
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }) {
        Text(
            "$label: ${
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(Date(value))
            }"
        )
    }
}

private enum class EndMode { ONE_DAY, RANGE, INDEFINITE }

@Composable
private fun EndModeSelector(selected: EndMode, onSelected: (EndMode) -> Unit) {
    Text(stringResource(R.string.label_schedule))
    EndMode.entries.forEach { mode ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected == mode, onClick = { onSelected(mode) })
            TextButton(onClick = { onSelected(mode) }) {
                Text(
                    stringResource(
                        when (mode) {
                            EndMode.ONE_DAY -> R.string.schedule_one_day; EndMode.RANGE -> R.string.schedule_date_range; EndMode.INDEFINITE -> R.string.schedule_indefinite
                        }
                    )
                )
            }
        }
    }
}
