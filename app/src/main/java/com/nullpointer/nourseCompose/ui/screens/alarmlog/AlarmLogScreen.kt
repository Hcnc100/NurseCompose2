package com.nullpointer.nourseCompose.ui.screens.alarmlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogEvent
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity
import com.nullpointer.nourseCompose.navigation.graph.HomeGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@HomeGraph
@Composable
fun AlarmLogScreen(
    destinationsNavigator: DestinationsNavigator,
    viewModel: AlarmLogViewModel = hiltViewModel(),
) {
    val logs by viewModel.logs.collectAsState()
    val firstRegistration = logs.lastOrNull { it.eventType == AlarmLogEvent.REMINDER_CREATED }
    val latestRegistration = logs.firstOrNull { it.eventType == AlarmLogEvent.REMINDER_CREATED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_alarm_logs)) },
                navigationIcon = { TextButton(onClick = destinationsNavigator::popBackStack) { Text(stringResource(R.string.action_back)) } },
            )
        },
    ) { padding ->
        if (logs.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.message_empty_alarm_logs), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(stringResource(R.string.title_alarm_log_summary), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.label_first_registration, firstRegistration?.reminderName ?: "—"), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.label_latest_registration, latestRegistration?.reminderName ?: "—"), style = MaterialTheme.typography.bodyMedium)
                }
                item { HorizontalDivider() }
                items(logs, key = { it.id }) { log -> AlarmLogRow(log) }
            }
        }
    }
}

@Composable
private fun AlarmLogRow(log: AlarmLogEntity) {
    val event = eventLabel(log.eventType)
    val statusColor = if (log.success) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    Card {
        ListItem(
            headlineContent = { Text(event) },
            supportingContent = {
                Column {
                    Text(log.reminderName)
                    Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(log.occurredAt)))
                    log.details?.let { Text(it) }
                    if (log.isFirstReminder) Text(stringResource(R.string.label_first_registration_badge), color = MaterialTheme.colorScheme.primary)
                }
            },
            trailingContent = { Text(if (log.success) stringResource(R.string.label_success) else stringResource(R.string.label_failure), color = statusColor) },
        )
    }
}

private fun eventLabel(eventType: String): String = when (eventType) {
    AlarmLogEvent.REMINDER_CREATED -> "Reminder registered"
    AlarmLogEvent.REMINDER_UPDATED -> "Reminder updated"
    AlarmLogEvent.REMINDER_ENABLED -> "Reminder enabled"
    AlarmLogEvent.REMINDER_DISABLED -> "Reminder cancelled"
    AlarmLogEvent.REMINDER_DELETED -> "Reminder deleted"
    AlarmLogEvent.ALARM_LAUNCHED -> "Alarm launched"
    AlarmLogEvent.FULL_SCREEN_ACTIVITY_OPENED -> "Full-screen alarm opened"
    AlarmLogEvent.ALARM_SCHEDULED -> "Alarm scheduled"
    AlarmLogEvent.ALARM_SCHEDULE_FAILED -> "Alarm scheduling failed"
    AlarmLogEvent.ALARM_FAILED -> "Alarm failed"
    AlarmLogEvent.ALARM_DISMISSED -> "Alarm marked as taken"
    AlarmLogEvent.ALARM_SNOOZED -> "Alarm snoozed"
    else -> eventType
}
