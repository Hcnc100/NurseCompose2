package com.nullpointer.nourseCompose.ui.screens.home.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.measure.MeasureRepository
import com.nullpointer.nourseCompose.domain.alarm.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val measureRepository: MeasureRepository,
    private val appLogger: AppLogger,
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val _message = Channel<Int>()
    val message = _message.receiveAsFlow()

    fun exportMeasureDatabase(outputStream: OutputStream) = viewModelScope.launch {
        isLoading = true

        runCatching {
            withContext(Dispatchers.IO) {
                outputStream.use {
                    measureRepository.exportDatabase(outputStream)
                }
            }
        }.onFailure {
            appLogger.error("Health data export", it.message ?: "Export failed", it)
            _message.trySend(R.string.error_export_measure)
        }

        isLoading = false
    }

    fun importMeasureDatabase(inputStream: InputStream) = viewModelScope.launch {
        isLoading = true

        runCatching {
            withContext(Dispatchers.IO) {
                inputStream.use {
                    measureRepository.importDatabase(inputStream)
                }
            }
        }.onFailure {
            appLogger.error("Health data import", it.message ?: "Import failed", it)
            _message.trySend(R.string.error_import_measure)
        }

        isLoading = false
    }

    fun deleterAllData() = viewModelScope.launch {
        isLoading = true

        runCatching {
            withContext(Dispatchers.IO) {
                measureRepository.deleterAllMeasures()
            }
        }.onFailure {
            appLogger.error("Health data deletion", it.message ?: "Deletion failed", it)
            _message.trySend(R.string.error_deleter_all_data)
        }

        isLoading = false
    }
}
