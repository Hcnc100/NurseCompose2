package com.nullpointer.nourseCompose.ui.screens.diagnostics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val repository: AlarmLogRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val logs = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _exportedReport = MutableSharedFlow<File>(extraBufferCapacity = 1)
    val exportedReport = _exportedReport.asSharedFlow()

    fun exportLogs() = viewModelScope.launch(Dispatchers.IO) {
        val directory = File(context.cacheDir, "logs").apply { mkdirs() }
        val report = File(directory, "nurseapp-diagnostics-${System.currentTimeMillis()}.txt")
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
        report.bufferedWriter().use { writer ->
            writer.appendLine("NurseApp diagnostics")
            writer.appendLine("Generated: ${dateFormat.format(Date())}")
            writer.appendLine("Logs: user-exported local diagnostics; Crashlytics remains the production crash source.")
            writer.appendLine()
            repository.observeAll().first().asReversed().forEach { log ->
                writer.appendLine("[${dateFormat.format(Date(log.occurredAt))}] ${log.severity} ${log.category} ${log.eventType}")
                writer.appendLine("Source: ${log.reminderName}")
                writer.appendLine("Status: ${if (log.success) "success" else "failure"}")
                log.details?.let { writer.appendLine("Details: $it") }
                log.stackTrace?.let { writer.appendLine("Stack trace:\n$it") }
                writer.appendLine("---")
            }
        }
        _exportedReport.emit(report)
    }
}
