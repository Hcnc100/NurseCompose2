package com.nullpointer.nourseCompose.ui.viewModel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.nourseCompose.models.data.MeasureData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SelectViewModel  @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel(

) {


     val measureSelected = mutableStateMapOf<Int, MeasureData>()


    fun clearSelection() {
        measureSelected.clear()
//        measureSelectedCount = 0
    }


    fun getAndClearSelection(): List<Int> {
        val map = measureSelected.keys.toList()
        clearSelection()
        return map
    }

    fun toggleMeasureData(measureData: MeasureData) {
        if (measureSelected.containsKey(measureData.id)) {
            measureSelected.remove(measureData.id)
        } else {
            measureSelected[measureData.id] = measureData
        }

//        measureSelectedCount = _measureSelected.size

    }
}