package com.nullpointer.nourseCompose.ui.screens.alarmlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmLogViewModel @Inject constructor(
    private val repository: AlarmLogRepository,
) : ViewModel() {
    val logs = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _saving = kotlinx.coroutines.flow.MutableStateFlow(false)
    val saving = _saving.asStateFlow()
    private val messages = kotlinx.coroutines.channels.Channel<Int>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val corrections = messages.receiveAsFlow()

    fun correctResponse(id: Long, expected: String, replacement: String) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            try {
                val changed = repository.correctResponse(id, expected, replacement)
                messages.send(if (changed) com.nullpointer.nourseCompose.R.string.history_corrected
                    else com.nullpointer.nourseCompose.R.string.history_correction_stale)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                messages.send(com.nullpointer.nourseCompose.R.string.history_correction_failed)
            } finally { _saving.value = false }
        }
    }
}
