package com.nullpointer.nourseCompose.ui.viewModel

import androidx.lifecycle.ViewModel
import com.nullpointer.nourseCompose.measure.MeasureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MeasureViewModel @Inject constructor(
    private val measureRepository: MeasureRepository
) : ViewModel() {


}