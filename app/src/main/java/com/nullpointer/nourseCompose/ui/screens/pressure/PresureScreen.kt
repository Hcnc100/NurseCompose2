package com.nullpointer.nourseCompose.ui.screens.pressure

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.paging.compose.collectAsLazyPagingItems
import com.nullpointer.nourseCompose.measureViewModelProvider
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.navigation.graph.HomeGraph
import com.nullpointer.nourseCompose.ui.screens.home.state.SelectedState
import com.nullpointer.nourseCompose.ui.share.MeasureScreen
import com.nullpointer.nourseCompose.ui.viewModel.MeasureViewModel
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@HomeGraph
@Composable
fun PressureScreen(
    lazyListState: LazyListState = rememberLazyListState(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    measureViewModel: MeasureViewModel = measureViewModelProvider(measureType = MeasureType.PRESSURE),
    selectedState: SelectedState
) {

    val lastMeasureList by measureViewModel.lastMeasureList.collectAsState()
    val pagingListMeasure = measureViewModel.listPagingMeasure.collectAsLazyPagingItems()

    DisposableEffect(key1 = Unit) {
        onDispose {
            measureViewModel.clearSelection()
            selectedState.changeNumberSelected(0)
        }
    }

    LaunchedEffect(key1 = selectedState.currentValueSelected) {
        if (selectedState.currentValueSelected == 0) {
            measureViewModel.clearSelection()
        }
    }

    LaunchedEffect(key1 = measureViewModel.measureSelectedCount) {
        selectedState.changeNumberSelected(measureViewModel.measureSelectedCount)
    }

    LaunchedEffect(key1 = Unit) {
        measureViewModel.message.collect {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }

    MeasureScreen(
        lazyListState = lazyListState,
        scaffoldState = scaffoldState,
        lastMeasureList = lastMeasureList,
        pagingListMeasure = pagingListMeasure,
        measureType = MeasureType.PRESSURE,
        addMeasureData = measureViewModel::addMeasureData,
        isSelectedEnable = measureViewModel.measureSelected.isNotEmpty(),
        addMeasureSelected = measureViewModel::toggleMeasureData,
        deleterMeasureSelected = measureViewModel::deleterAllSelected,
        listMeasureSelected = measureViewModel.measureSelected
    )
}