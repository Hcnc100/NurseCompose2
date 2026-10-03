package com.nullpointer.nourseCompose.ui.screens.alarmlog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.domain.alarm.MedicationHistory
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import com.nullpointer.nourseCompose.navigation.graph.HomeGraph
import com.nullpointer.nourseCompose.ui.share.AppTopBar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Destination
@HomeGraph
@Composable
fun AlarmLogScreen(
    destinationsNavigator: DestinationsNavigator,
    technical: Boolean = false,
    viewModel: AlarmLogViewModel = hiltViewModel(),
) {
    val logs by viewModel.logs.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val snackbars = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.corrections.collect { snackbars.showSnackbar(context.getString(it)) }
    }
    AlarmHistoryContent(logs, technical, onBack = { destinationsNavigator.popBackStack() },
        onCorrection = viewModel::correctResponse, saving = saving, snackbars = snackbars)
}

/** Technical details are opt-in from Settings, never mixed with patient confirmations. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AlarmHistoryContent(logs: List<AlarmLogEntity>, technical: Boolean, onBack: () -> Unit,
    onCorrection: ((Long, String, String) -> Unit)? = null, saving: Boolean = false,
    snackbars: SnackbarHostState = remember { SnackbarHostState() }) {
    var correctionId by rememberSaveable { mutableStateOf<Long?>(null) }
    var expectedResponse by rememberSaveable { mutableStateOf<String?>(null) }
    var period by rememberSaveable { mutableStateOf(7) }
    var fromPicker by rememberSaveable { mutableStateOf<Long?>(null) }
    var untilPicker by rememberSaveable { mutableStateOf<Long?>(null) }
    var medication by rememberSaveable { mutableStateOf<String?>(null) }
    var showDates by rememberSaveable { mutableStateOf(false) }
    var showMedications by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val locale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    val dayFormat = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
    val timeFormat = remember(locale) { android.text.format.DateFormat.getTimeFormat(context) }
    val now = System.currentTimeMillis()
    val from = when (period) {
        1 -> MedicationHistory.startOfDay(now)
        7 -> MedicationHistory.daysBefore(now, 6)
        -1 -> fromPicker?.let { MedicationHistory.pickerDay(it) }
        else -> null
    }
    val until = when (period) {
        1, 7 -> MedicationHistory.daysBefore(now, -1)
        -1 -> untilPicker?.let { MedicationHistory.pickerDay(it, followingDay = true) }
        else -> null
    }
    val visible = if (technical) logs else MedicationHistory.filter(logs, from, until, medication)
    val groups = visible.groupBy { MedicationHistory.startOfDay(it.occurredAt) }
    Scaffold(snackbarHost = { SnackbarHost(snackbars) }, topBar = {
        AppTopBar(stringResource(if (technical) R.string.title_technical_alarm_log else R.string.title_medication_history), onBack = onBack)
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(stringResource(if (technical) R.string.settings_support_description else R.string.history_explanation),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!technical) item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to R.string.history_today, 7 to R.string.history_week, 0 to R.string.history_all).forEach { (value, label) ->
                        FilterChip(selected = period == value, onClick = { period = value }, label = { Text(stringResource(label)) })
                    }
                    FilterChip(selected = period == -1, onClick = { showDates = true }, label = { Text(stringResource(R.string.history_custom)) })
                }
                if (period == -1 && from != null && untilPicker != null) {
                    Text(stringResource(R.string.history_custom_range, dayFormat.format(Date(from)),
                        dayFormat.format(Date(MedicationHistory.pickerDay(untilPicker!!)))), style = MaterialTheme.typography.bodyMedium)
                }
                Box {
                    OutlinedButton(onClick = { showMedications = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.history_filter_medication, medication ?: stringResource(R.string.history_all_medications)),
                            modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMedications)
                    }
                    DropdownMenu(expanded = showMedications, onDismissRequest = { showMedications = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.history_all_medications)) },
                            onClick = { medication = null; showMedications = false })
                        logs.filter(MedicationHistory::isVisible).map { it.reminderName }.distinct().sorted().forEach { name ->
                            DropdownMenuItem(text = { Text(name.ifBlank { stringResource(R.string.history_unknown_name) }) },
                                onClick = { medication = name; showMedications = false })
                        }
                    }
                }
            }
            if (visible.isEmpty()) item {
                Text(stringResource(if (technical) R.string.message_empty_alarm_logs else R.string.history_empty),
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 24.dp))
                if (!technical && (period != 0 || medication != null)) {
                    TextButton(onClick = { period = 0; medication = null }) {
                        Text(stringResource(R.string.history_clear_filters))
                    }
                }
            }
            groups.forEach { (day, entries) ->
                item(key = "day-$day") {
                    Text(dayFormat.format(Date(day)), style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() })
                }
                items(entries, key = { it.id }) { log ->
                    Card(shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(log.reminderName.ifBlank { stringResource(R.string.history_unknown_name) },
                                style = MaterialTheme.typography.titleMedium)
                            Text(timeFormat.format(Date(log.occurredAt)), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val label = if (!log.success && log.eventType in setOf(AlarmLogEvent.MEDICATION_TAKEN, AlarmLogEvent.MEDICATION_NOT_TAKEN)) R.string.history_confirmation_failed
                                else if (!log.success && log.eventType == AlarmLogEvent.ALARM_LAUNCHED) R.string.history_event_failed
                                else eventTitleResource(log.eventType)
                            Text(if (label != null) stringResource(label) else log.eventType,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (!log.success) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                            if (!technical) {
                                val note = when {
                                    MedicationHistory.confirmsIntake(log) -> R.string.history_confirmation_note
                                    log.success && log.eventType == AlarmLogEvent.MEDICATION_NOT_TAKEN -> R.string.history_not_taken_note
                                    else -> R.string.history_notice_note
                                }
                                Text(stringResource(note),
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (onCorrection != null && MedicationHistory.isExplicitResponse(log)) {
                                    TextButton(enabled = !saving, onClick = {
                                        correctionId = log.id; expectedResponse = log.eventType
                                    }) { Text(stringResource(R.string.history_correct_response)) }
                                }
                            } else {
                                Text(stringResource(if (log.success) R.string.label_success else R.string.label_failure))
                                log.details?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                                log.stackTrace?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }
    }
    val correction = logs.firstOrNull { it.id == correctionId }
    if (correction != null && expectedResponse != null && onCorrection != null) {
        val replacement = if (expectedResponse == AlarmLogEvent.MEDICATION_TAKEN)
            AlarmLogEvent.MEDICATION_NOT_TAKEN else AlarmLogEvent.MEDICATION_TAKEN
        androidx.compose.ui.window.Dialog(onDismissRequest = { correctionId = null }) {
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides configuration, LocalDensity provides density) {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(R.string.history_correct_response), style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.semantics { heading() })
                            Text(correction.reminderName.ifBlank { stringResource(R.string.history_unknown_name) },
                                style = MaterialTheme.typography.titleMedium)
                            Text("${dayFormat.format(Date(correction.occurredAt))} · ${timeFormat.format(Date(correction.occurredAt))}")
                            Text(stringResource(R.string.history_correction_message,
                                stringResource(if (replacement == AlarmLogEvent.MEDICATION_TAKEN) R.string.history_response_taken
                                    else R.string.history_response_not_taken)))
                        }
                        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End)) {
                            TextButton(onClick = { correctionId = null }) { Text(stringResource(R.string.message_cancel_dialog)) }
                            Button(enabled = !saving, onClick = {
                                onCorrection(correction.id, expectedResponse!!, replacement)
                                correctionId = null
                            }) { Text(stringResource(R.string.history_save_correction), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
                        }
                    }
                }
            }
        }
    }
    if (showDates) {
        val state = rememberDateRangePickerState(initialSelectedStartDateMillis = fromPicker, initialSelectedEndDateMillis = untilPicker)
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDates = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            // A separate Android window installs its own locals. Preserve scoped language and text scale.
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides configuration, LocalDensity provides density) {
            Scaffold(topBar = { AppTopBar(stringResource(R.string.history_custom), onBack = { showDates = false }) },
                bottomBar = {
                    FlowRow(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End)) {
                        TextButton(onClick = { showDates = false }) { Text(stringResource(R.string.message_cancel_dialog)) }
                        Button(enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null, onClick = {
                            fromPicker = state.selectedStartDateMillis; untilPicker = state.selectedEndDateMillis
                            period = -1; showDates = false
                        }) { Text(stringResource(R.string.history_apply)) }
                    }
                }) { padding ->
                    DateRangePicker(state, modifier = Modifier.fillMaxSize().padding(padding), title = null,
                        headline = {
                            val shortDate = remember(locale) { DateFormat.getDateInstance(DateFormat.SHORT, locale) }
                            val start = state.selectedStartDateMillis?.let { shortDate.format(Date(MedicationHistory.pickerDay(it))) }
                                ?: stringResource(R.string.history_range_start)
                            val end = state.selectedEndDateMillis?.let { shortDate.format(Date(MedicationHistory.pickerDay(it))) }
                                ?: stringResource(R.string.history_range_end)
                            Text("$start — $end", style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp))
                        })
                }
            }
        }
    }
}

internal fun eventTitleResource(event: String): Int? = when (event) {
    AlarmLogEvent.MEDICATION_TAKEN -> R.string.history_event_taken
    AlarmLogEvent.MEDICATION_NOT_TAKEN -> R.string.history_event_not_taken
    AlarmLogEvent.MEDICATION_RESPONSE_CORRECTED -> R.string.history_event_corrected_audit
    AlarmLogEvent.ALARM_LAUNCHED -> R.string.history_event_launched
    AlarmLogEvent.ALARM_DISMISSED -> R.string.history_event_dismissed
    AlarmLogEvent.ALARM_SNOOZED -> R.string.history_event_snoozed
    AlarmLogEvent.ALARM_FAILED -> R.string.history_event_failed
    AlarmLogEvent.ALARM_SCHEDULE_FAILED -> R.string.history_event_schedule_failed
    AlarmLogEvent.REMINDER_CREATED -> R.string.history_event_created
    AlarmLogEvent.REMINDER_UPDATED -> R.string.history_event_updated
    AlarmLogEvent.REMINDER_ENABLED -> R.string.history_event_enabled
    AlarmLogEvent.REMINDER_DISABLED -> R.string.history_event_disabled
    AlarmLogEvent.REMINDER_DELETED -> R.string.history_event_deleted
    AlarmLogEvent.ALARM_SCHEDULED -> R.string.history_event_scheduled
    AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED -> R.string.history_event_full_screen
    AlarmLogEvent.FOREGROUND_ALARM_ACTIVITY_REQUESTED -> R.string.history_event_foreground
    else -> null
}
