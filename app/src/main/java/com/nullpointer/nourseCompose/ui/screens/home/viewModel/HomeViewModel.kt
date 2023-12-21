package com.nullpointer.nourseCompose.ui.screens.home.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.domain.measure.MeasureRepository
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
    private val measureRepository: MeasureRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val _message = Channel<String>()
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
            _message.trySend("Error in export file, verify your file")
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
            _message.trySend("Error in export $it")
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
            _message.trySend("Error in export $it")
        }

        isLoading = false
    }
}