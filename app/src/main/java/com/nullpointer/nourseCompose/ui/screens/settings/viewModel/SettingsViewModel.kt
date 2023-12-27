package com.nullpointer.nourseCompose.ui.screens.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.domain.settings.SettingsRepository
import com.nullpointer.nourseCompose.models.data.SettingsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {


    val settingsData = settingsRepository.getMeasureSettingsData().catch {
        emit(null)
    }.flowOn(
        Dispatchers.IO
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )


    fun updateNumberMeasureGraph(number: Int) = viewModelScope.launch {
        val updateSettings =
            (settingsData.value ?: SettingsData()).copy(numberMeasureGraph = number)
        withContext(Dispatchers.IO) {
            settingsRepository.saveMeasureSettingsData(updateSettings)
        }
    }
}