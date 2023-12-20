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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val measureRepository: MeasureRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val _message = Channel<String>()
    val message = _message.receiveAsFlow()

    fun exportMeasureDatabase(file: File) = viewModelScope.launch {
        isLoading = true

        runCatching {
            withContext(Dispatchers.IO) {
                measureRepository.exportDatabase(file)
            }
        }.onFailure {
            _message.trySend("Error in export $it")
        }

        isLoading = false
    }

    fun importMeasureDatabase(file: File) = viewModelScope.launch {
        isLoading = true

        runCatching {
            withContext(Dispatchers.IO) {
                measureRepository.importDatabase(file)
            }
        }.onFailure {
            _message.trySend("Error in export $it")
        }

        isLoading = false
    }
}