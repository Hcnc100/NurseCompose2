package com.nullpointer.nourseCompose.ui.screens.medication

import android.content.Intent
import com.nullpointer.nourseCompose.domain.medication.ReminderIntervalUnit
import android.app.NotificationManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Surface
import androidx.compose.material3.CardDefaults
import com.nullpointer.nourseCompose.ui.share.AppTopBar
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
import com.nullpointer.nourseCompose.navigation.graph.HomeGraph
import com.nullpointer.nourseCompose.reports.MedicationReportExporter
import com.nullpointer.nourseCompose.ui.screens.destinations.MedicationReminderEditorScreenDestination
import com.nullpointer.nourseCompose.navigation.LocalRootNavController
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
    val reminders by viewModel.reminders.collectAsState()
    val nextAlarmTimes by viewModel.nextAlarmTimes.collectAsState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1_000)
        }
    }
    val rootNavController = LocalRootNavController.current
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
                    rootNavController.navigate(MedicationReminderEditorScreenDestination().route)
                },
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(painterResource(R.drawable.baseline_add_24), contentDescription = stringResource(R.string.action_add_medication)) }
        },
    ) { padding ->
        if (reminders.isEmpty()) {
            EmptyMedicationState(modifier = Modifier.padding(padding).fillMaxSize())
        } else {
            Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(bottom = 88.dp)) {
                reminders.forEach { reminder ->
                    MedicationReminderCard(reminder = reminder, now = now, scheduledAt = nextAlarmTimes[reminder.id], onClick = {
                        rootNavController.navigate(
                            MedicationReminderEditorScreenDestination(reminderId = reminder.id).route
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
internal fun MedicationReminderCard(reminder: MedicationReminderEntity, now: Long, scheduledAt: Long?, onClick: () -> Unit, onActiveChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    val locale = androidx.core.os.ConfigurationCompat.getLocales(
        androidx.compose.ui.platform.LocalConfiguration.current,
    )[0] ?: java.util.Locale.getDefault()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(reminder.name, style = MaterialTheme.typography.titleLarge)
                reminder.dosage?.takeIf(String::isNotBlank)?.let { Text(it) }
                if (reminder.endAt == reminder.startAt) {
                    Text(stringResource(R.string.schedule_single_dose), modifier = Modifier.fillMaxWidth())
                } else {
                    Text(stringResource(R.string.label_every_minutes, reminder.intervalMinutes), modifier = Modifier.fillMaxWidth())
                }
                Text(if (reminder.isActive) stringResource(R.string.label_active) else stringResource(R.string.label_inactive), style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(checked = reminder.isActive, onCheckedChange = onActiveChange)
                IconButton(onClick = onDelete) { Icon(painterResource(R.drawable.baseline_delete_24), contentDescription = stringResource(R.string.action_delete)) }
            }
        }
        // Full card width, rather than the narrow column beside the controls: allow
        // both translated labels and large system text to wrap without clipping.
        val nextAt = com.nullpointer.nourseCompose.domain.medication.ReminderNextAlarm.at(reminder, now, scheduledAt)
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Text(stringResource(if (reminder.isActive && nextAt != null && scheduledAt != nextAt)
                R.string.label_next_reminder_dose else R.string.label_next_reminder_alarm), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                when {
                    !reminder.isActive -> stringResource(R.string.label_reminder_alarm_paused)
                    nextAt == null -> stringResource(R.string.label_reminder_alarm_finished)
                    else -> DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(Date(nextAt))
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
fun MedicationReminderEditor(
    reminder: MedicationReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (MedicationReminderEntity) -> Unit,
    isSaving: Boolean = false,
    saveError: Boolean = false,
) {
    val context = LocalContext.current
    val dateTimeFormatter = reminderDateTimeFormatter()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    var name by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.name.orEmpty()) }
    var dosage by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.dosage.orEmpty()) }
    var comment by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.comment.orEmpty()) }
    var photoUri by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.photoUri) }
    var startAt by rememberSaveable(reminder?.id) {
        mutableStateOf(
            reminder?.startAt ?: System.currentTimeMillis()
        )
    }
    var endMode by rememberSaveable(reminder?.id) { mutableStateOf(when {
        reminder?.endAt == null -> EndMode.INDEFINITE
        reminder.endAt == reminder.startAt -> EndMode.SINGLE_DOSE
        reminder.endAt == com.nullpointer.nourseCompose.domain.medication.ReminderDuration.endOfStartDay(reminder.startAt) -> EndMode.ONE_DAY
        else -> EndMode.RANGE
    }) }
    var endAt by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.endAt ?: startAt) }
    var intervalUnit by rememberSaveable(reminder?.id) {
        mutableStateOf(ReminderIntervalUnit.forMinutes(reminder?.intervalMinutes ?: 60))
    }
    var intervalText by rememberSaveable(reminder?.id) {
        mutableStateOf(
            intervalUnit.format(reminder?.intervalMinutes ?: 60)
        )
    }
    var vibrationEnabled by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.vibrationEnabled ?: false) }
    var soundEnabled by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.soundEnabled ?: false) }
    var fullScreenAlarm by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.fullScreenAlarm ?: false) }
    val draft = listOf(name, dosage, comment, photoUri.orEmpty(), startAt.toString(), endMode.name,
        endAt.toString(), intervalText, intervalUnit.name, vibrationEnabled.toString(), soundEnabled.toString(), fullScreenAlarm.toString())
    val originalDraft = rememberSaveable(reminder?.id) { draft }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val requestLeave: () -> Unit = {
        if (!isSaving) {
            if (draft != originalDraft) showDiscardDialog = true else onDismiss()
        }
    }
    androidx.activity.compose.BackHandler { requestLeave() }
    var nameError by remember { mutableStateOf(false) }
    var intervalError by remember { mutableStateOf(false) }
    var startError by remember { mutableStateOf(false) }
    val startBringIntoView = remember { androidx.compose.foundation.relocation.BringIntoViewRequester() }
    LaunchedEffect(startError) { if (startError) startBringIntoView.bringIntoView() }
    var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    LaunchedEffect(startAt) {
        if (endAt < startAt) endAt = startAt
    }
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
    val preview = remember(startAt, endMode, endAt, intervalText, intervalUnit) {
        val normalizedEndAt = endAt.coerceAtLeast(startAt)
        intervalUnit.toMinutes(intervalText)?.let { interval ->
            ReminderSchedule.occurrencesBetween(
                MedicationReminderEntity(
                    name = "preview",
                    startAt = startAt,
                    endAt = when (endMode) {
                        EndMode.INDEFINITE -> null
                        EndMode.SINGLE_DOSE -> startAt
                        EndMode.ONE_DAY -> com.nullpointer.nourseCompose.domain.medication.ReminderDuration.endOfStartDay(startAt)
                        EndMode.RANGE -> normalizedEndAt
                    },
                    intervalHours = maxOf(1, interval / 60),
                    intervalMinutes = interval,
                ),
                System.currentTimeMillis(), System.currentTimeMillis() + 48 * 60 * 60 * 1_000L,
            ).take(3)
        }.orEmpty()
    }

    val saveReminder = {
        // A hidden recurrence field must not prevent a single-dose reminder from saving.
        // Keep valid stored/draft values; the fallback is unused by its one-occurrence schedule.
        val interval = if (endMode == EndMode.SINGLE_DOSE) {
            intervalUnit.toMinutes(intervalText) ?: 60
        } else intervalUnit.toMinutes(intervalText)
        val invalidSingleDoseTime = endMode == EndMode.SINGLE_DOSE &&
            com.nullpointer.nourseCompose.domain.medication.ReminderDuration.requiresFutureTime(
                startAt, reminder?.startAt, reminder?.endAt, System.currentTimeMillis())
        if (name.isBlank() || interval == null || interval <= 0 || invalidSingleDoseTime) {
            nameError = name.isBlank()
            intervalError = interval == null || interval <= 0
            startError = invalidSingleDoseTime
        } else onSave(
            MedicationReminderEntity(
                id = reminder?.id ?: 0,
                name = name.trim(),
                dosage = dosage.ifBlank { null },
                comment = comment.ifBlank { null },
                photoUri = photoUri,
                startAt = startAt,
                endAt = when (endMode) {
                    EndMode.INDEFINITE -> null
                    EndMode.SINGLE_DOSE -> startAt
                    EndMode.ONE_DAY -> com.nullpointer.nourseCompose.domain.medication.ReminderDuration.endOfStartDay(startAt)
                    EndMode.RANGE -> endAt.coerceAtLeast(startAt)
                },
                intervalHours = maxOf(1, interval / 60),
                intervalMinutes = interval,
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
            AppTopBar(
                title = stringResource(if (reminder == null) R.string.title_add_medication else R.string.title_edit_medication),
                onBack = requestLeave,
            )
        },
        bottomBar = {
            // Reserve space for Save instead of floating over the fields and dose preview.
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Button(
                    onClick = saveReminder,
                    enabled = !isSaving,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_check_24),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        stringResource(if (isSaving) R.string.reminder_saving else R.string.action_save),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (saveError) Text(stringResource(R.string.reminder_save_failed), color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite })
            OutlinedTextField(
                name,
                { name = it; nameError = false },
                label = { Text(stringResource(R.string.label_medication_name)) },
                isError = nameError,
                supportingText = if (nameError) { { Text(stringResource(R.string.error_medication_name)) } } else null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                dosage,
                { dosage = it },
                label = { Text(stringResource(R.string.label_dosage_optional)) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                comment,
                { comment = it },
                label = { Text(stringResource(R.string.label_comment_optional)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { showPhotoSheet = true }, shape = MaterialTheme.shapes.medium) { Text(stringResource(R.string.action_select_photo)) }
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
            Column(Modifier.fillMaxWidth().bringIntoViewRequester(startBringIntoView)) {
            DateTimeButton(
                stringResource(R.string.label_start_time),
                startAt,
                onChange = { startAt = it; startError = false })
            if (startError) Text(
                stringResource(R.string.error_single_dose_time),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite },
            )
            }
            EndModeSelector(endMode, {
                endMode = it
                intervalError = false
                startError = false
                focusManager.clearFocus()
                keyboardController?.hide()
            })
            ReminderConditionalSection(endMode == EndMode.ONE_DAY) {
                Text(stringResource(R.string.description_schedule_one_day), style = MaterialTheme.typography.bodySmall)
            }
            ReminderConditionalSection(endMode == EndMode.RANGE) {
                DateTimeButton(stringResource(R.string.label_end_date), endAt.coerceAtLeast(startAt),
                    onChange = { endAt = it })
            }
            ReminderConditionalSection(endMode != EndMode.SINGLE_DOSE) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderIntervalUnit.values().forEach { unit ->
                    androidx.compose.material3.FilterChip(
                        selected = intervalUnit == unit,
                        onClick = {
                            if (intervalUnit != unit) {
                                intervalUnit.toMinutes(intervalText)?.let { intervalText = unit.format(it) }
                                intervalUnit = unit
                                intervalError = false
                            }
                        },
                        label = { Text(stringResource(if (unit == ReminderIntervalUnit.MINUTES)
                            R.string.interval_unit_minutes else R.string.interval_unit_hours)) },
                    )
                }
            }
            OutlinedTextField(
                intervalText,
                { intervalText = it; intervalError = false },
                label = { Text(stringResource(if (intervalUnit == ReminderIntervalUnit.MINUTES)
                    R.string.label_interval_hours else R.string.label_interval_in_hours)) },
                isError = intervalError,
                supportingText = when {
                    intervalError -> { { Text(stringResource(R.string.error_medication_interval)) } }
                    intervalUnit == ReminderIntervalUnit.HOURS -> { { Text(stringResource(R.string.interval_hours_hint)) } }
                    else -> null
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = if (intervalUnit == ReminderIntervalUnit.HOURS)
                        androidx.compose.ui.text.input.KeyboardType.Decimal else androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            }
            Text(stringResource(R.string.label_notification_behavior), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = soundEnabled, role = Role.Switch, onValueChange = { soundEnabled = it }
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = soundEnabled, onCheckedChange = null)
                Text(stringResource(R.string.option_notification_sound), modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = vibrationEnabled, role = Role.Switch, onValueChange = { vibrationEnabled = it }
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = vibrationEnabled, onCheckedChange = null)
                Text(stringResource(R.string.option_notification_vibration), modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = fullScreenAlarm, role = Role.Switch, onValueChange = { fullScreenAlarm = it }
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = fullScreenAlarm, onCheckedChange = null)
                Text(stringResource(R.string.option_full_screen_alarm), modifier = Modifier.padding(start = 8.dp))
            }
            ReminderConditionalSection(fullScreenAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                !context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
            ) {
                Text(stringResource(R.string.intro_full_screen_permission))
                TextButton(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                            .setData(Uri.parse("package:${context.packageName}"))
                    )
                }) { Text(stringResource(R.string.action_enable_full_screen_permission)) }
            }
            ReminderConditionalSection(endMode != EndMode.SINGLE_DOSE) {
            Text(
                stringResource(R.string.label_next_doses),
                style = MaterialTheme.typography.titleMedium
            )
            Text(preview.joinToString("\n") {
                dateTimeFormatter.format(Date(it))
            }.ifBlank { stringResource(R.string.message_no_upcoming_doses) })
            }
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

    if (showDiscardDialog) AlertDialog(
        onDismissRequest = { showDiscardDialog = false },
        title = { Text(stringResource(R.string.reminder_discard_title)) },
        text = { Text(stringResource(R.string.reminder_discard_message)) },
        confirmButton = { TextButton(onClick = { showDiscardDialog = false; onDismiss() }) {
            Text(stringResource(R.string.reminder_discard_action))
        } },
        dismissButton = { TextButton(onClick = { showDiscardDialog = false }) {
            Text(stringResource(R.string.reminder_keep_editing))
        } },
    )
    if (isSaving) AlertDialog(
        onDismissRequest = {},
        text = { Text(stringResource(R.string.reminder_saving)) },
        confirmButton = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeButton(label: String, value: Long, onChange: (Long) -> Unit) {
    val dateTimeFormatter = reminderDateTimeFormatter()
    val calendar = remember(value) { Calendar.getInstance().apply { timeInMillis = value } }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = ReminderDateSelection.pickerDate(value))
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = android.text.format.DateFormat.is24HourFormat(LocalContext.current)
    )

    TextButton(onClick = {
        datePickerState.selectedDateMillis = ReminderDateSelection.pickerDate(value)
        timePickerState.hour = calendar.get(Calendar.HOUR_OF_DAY)
        timePickerState.minute = calendar.get(Calendar.MINUTE)
        showDatePicker = true
    }) {
        Text(
            "$label: ${
                dateTimeFormatter
                    .format(Date(value))
            }"
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) { Text(stringResource(R.string.intro_next)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.button_cancel_title)) }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = true) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(label) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    onChange(ReminderDateSelection.localDateTime(
                        datePickerState.selectedDateMillis ?: ReminderDateSelection.pickerDate(value),
                        timePickerState.hour, timePickerState.minute,
                    ))
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_confirm_time)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.button_cancel_title)) }
            }
        )
    }
}

private enum class EndMode { SINGLE_DOSE, ONE_DAY, RANGE, INDEFINITE }

@Composable
private fun ReminderConditionalSection(visible: Boolean, content: @Composable () -> Unit) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.expandVertically(
            animationSpec = androidx.compose.animation.core.tween(200)
        ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200)),
        exit = androidx.compose.animation.shrinkVertically(
            animationSpec = androidx.compose.animation.core.tween(200)
        ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
private fun EndModeSelector(selected: EndMode, onSelected: (EndMode) -> Unit) {
    Text(stringResource(R.string.label_schedule), style = MaterialTheme.typography.titleMedium)
    Column(Modifier.selectableGroup()) {
        EndMode.entries.forEach { mode ->
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(
                    selected = selected == mode,
                    role = Role.RadioButton,
                    onClick = { onSelected(mode) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected == mode, onClick = null)
                Text(
                    stringResource(
                        when (mode) {
                            EndMode.SINGLE_DOSE -> R.string.schedule_single_dose
                            EndMode.ONE_DAY -> R.string.schedule_one_day
                            EndMode.RANGE -> R.string.schedule_date_range
                            EndMode.INDEFINITE -> R.string.schedule_indefinite
                        }
                    ),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun reminderDateTimeFormatter(): java.text.DateFormat {
    val locale = androidx.core.os.ConfigurationCompat.getLocales(
        androidx.compose.ui.platform.LocalConfiguration.current
    )[0] ?: java.util.Locale.getDefault()
    val use24Hour = android.text.format.DateFormat.is24HourFormat(LocalContext.current)
    return java.text.SimpleDateFormat(
        android.text.format.DateFormat.getBestDateTimePattern(locale, if (use24Hour) "yMdHm" else "yMdhm"),
        locale,
    )
}
