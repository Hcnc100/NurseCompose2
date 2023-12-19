package com.nullpointer.nourseCompose.inject.viewModel.measure

import com.nullpointer.nourseCompose.ui.viewModel.MeasureViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent


@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryProvider {

    fun measureViewModelFactory(): MeasureViewModel.Factory

}