package com.nullpointer.nourseCompose.ui.screens.alarmlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmLogViewModel @Inject constructor(
    repository: AlarmLogRepository,
) : ViewModel() {
    val logs = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
