package com.nullpointer.nourseCompose.notifications

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import com.nullpointer.nourseCompose.ui.theme.AppDarkColorScheme
import com.nullpointer.nourseCompose.ui.theme.AppLightColorScheme
import com.nullpointer.nourseCompose.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@AndroidEntryPoint
class MedicationAlarmActivity : ComponentActivity() {
    @Inject lateinit var alarmLogRepository: AlarmLogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyApplication)
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val barStyle = SystemBarStyle.auto(
            AppLightColorScheme.surface.toArgb(), AppDarkColorScheme.surface.toArgb(),
        )
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        val id = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1)
        val name = intent.getStringExtra("reminder_name").orEmpty()
        val dosage = intent.getStringExtra("reminder_dosage").orEmpty()
        val photo = intent.getStringExtra("reminder_photo")
        logEvent(AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED,
            "type=MEDICATION; fullScreenActivityOpened=true; showWhenLocked=true; turnScreenOn=true")
        setContent {
            MyApplicationTheme {
                var confirmNotTaken by rememberSaveable { mutableStateOf(false) }
                var decisionPending by remember { mutableStateOf(false) }
                var decisionFailed by remember { mutableStateOf(false) }
                // Do not abandon the confirmation while Room is committing it.
                BackHandler(enabled = decisionPending) { }
                fun saveDecision(event: String) {
                    if (decisionPending) return
                    decisionPending = true
                    decisionFailed = false
                    confirmNotTaken = false
                    recordDecisionAndStop(event) {
                        decisionPending = false
                        decisionFailed = true
                    }
                }
                val bitmap = remember(photo) {
                    photo?.let {
                        runCatching {
                            contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream)
                        }.getOrNull()
                    }
                }
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
                        contentAlignment = Alignment.TopCenter) {
                    Column(
                        Modifier.widthIn(max = 600.dp).fillMaxSize().padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Information scrolls independently so large text cannot hide the actions.
                        Column(
                            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            Icon(painterResource(R.drawable.baseline_alarm_24), contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                            Text(stringResource(R.string.title_alarm_now),
                                style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().semantics { heading() })
                            if (bitmap != null) {
                                Card(shape = MaterialTheme.shapes.large) {
                                    Image(bitmap.asImageBitmap(), contentDescription = null,
                                        modifier = Modifier.size(120.dp), contentScale = ContentScale.Crop)
                                }
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            ) {
                                Column(Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(name.ifBlank { stringResource(R.string.title_alarm_now) },
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.fillMaxWidth().semantics { heading() })
                                    if (dosage.isNotBlank()) {
                                        Text(dosage, style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                                enabled = !decisionPending,
                                onClick = {
                                    saveDecision(AlarmLogEvent.MEDICATION_TAKEN)
                                },
                            ) {
                                Text(stringResource(R.string.action_taken_alarm), textAlign = TextAlign.Center)
                            }
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                                enabled = !decisionPending,
                                onClick = {
                                    logEvent(AlarmLogEvent.ALARM_SNOOZED, "Alarm snoozed for 10 minutes")
                                    startService(Intent(this@MedicationAlarmActivity, MedicationAlarmService::class.java)
                                        .setAction(MedicationAlarmService.SNOOZE)
                                        .putExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, id))
                                    finishAndRemoveTask()
                                },
                            ) {
                                Text(stringResource(R.string.action_snooze_alarm), textAlign = TextAlign.Center)
                            }
                            TextButton(onClick = { confirmNotTaken = true }, enabled = !decisionPending,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
                                Text(stringResource(R.string.action_not_taken_alarm), textAlign = TextAlign.Center)
                            }
                            if (decisionPending) LinearProgressIndicator(Modifier.fillMaxWidth())
                            if (decisionFailed) Text(stringResource(R.string.alarm_decision_save_failed),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite })
                        }
                    }
                    }
                }
                if (confirmNotTaken) AlertDialog(
                    onDismissRequest = { confirmNotTaken = false },
                    title = { Text(stringResource(R.string.alarm_not_taken_title)) },
                    text = { Text(stringResource(R.string.alarm_not_taken_message),
                        modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) },
                    confirmButton = { TextButton(onClick = { saveDecision(AlarmLogEvent.MEDICATION_NOT_TAKEN) }) {
                        Text(stringResource(R.string.alarm_not_taken_confirm))
                    } },
                    dismissButton = { TextButton(onClick = { confirmNotTaken = false }) {
                        Text(stringResource(R.string.message_cancel_dialog))
                    } },
                )
            }
        }
    }

    private fun recordDecisionAndStop(eventType: String, onFailure: () -> Unit) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    alarmLogRepository.record(AlarmLogEntity(
                        reminderId = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1),
                        reminderName = intent.getStringExtra("reminder_name").orEmpty(),
                        eventType = eventType, success = true, details = "Explicit user response to this alarm",
                    ))
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                onFailure()
                return@launch
            }
            val id = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1)
            startService(Intent(this@MedicationAlarmActivity, MedicationAlarmService::class.java)
                .setAction(MedicationAlarmService.STOP)
                .putExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, id))
            NotificationManagerCompat.from(this@MedicationAlarmActivity).cancel(id.toInt())
            finishAndRemoveTask()
        }
    }

    private fun logEvent(eventType: String, details: String) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmLogRepository.record(AlarmLogEntity(
                reminderId = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1),
                reminderName = intent.getStringExtra("reminder_name").orEmpty(),
                eventType = eventType, success = true, details = details,
            ))
        }
    }
}
