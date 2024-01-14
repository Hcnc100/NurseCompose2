package com.nullpointer.nourseCompose.ui.screens.temperature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.paging.compose.collectAsLazyPagingItems
import com.nullpointer.nourseCompose.measureViewModelProvider
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.navigation.graph.HomeGraph
import com.nullpointer.nourseCompose.state.MeasureScreenState
import com.nullpointer.nourseCompose.state.rememberMeasureScreenState
import com.nullpointer.nourseCompose.ui.share.MeasureScreen
import com.nullpointer.nourseCompose.ui.viewModel.MeasureViewModel
import com.nullpointer.nourseCompose.ui.viewModel.SelectViewModel
import com.ramcosta.composedestinations.annotation.Destination


@Destination
@HomeGraph(start = true)
@Composable
fun TemperatureScreen(
    selectViewModel: SelectViewModel,
    measureScreenState: MeasureScreenState = rememberMeasureScreenState(),
    measureViewModel: MeasureViewModel = measureViewModelProvider(measureType = MeasureType.TEMPERATURE),
) {

    val lastMeasureList by measureViewModel.lastMeasureList.collectAsState()
    val pagingListMeasure = measureViewModel.listPagingMeasure.collectAsLazyPagingItems()



    LaunchedEffect(key1 = Unit) {
        measureViewModel.message.collect(measureScreenState::showSnackMessage)
    }

    MeasureScreen(
        lazyGridState = measureScreenState.lazyGridState,
        scaffoldState = measureScreenState.scaffoldState,
        lastMeasureList = lastMeasureList,
        pagingListMeasure = pagingListMeasure,
        measureType = MeasureType.TEMPERATURE,
        addMeasureData = measureViewModel::addMeasureData,
        isSelectedEnable = selectViewModel.measureSelected.isNotEmpty(),
        addMeasureSelected = selectViewModel::toggleMeasureData,
        listMeasureSelected = selectViewModel.measureSelected,
        deleterMeasureSelected = {
            selectViewModel.getAndClearSelection().let { list ->
                measureViewModel.deleterAllSelected(list)
            }
        },
    )
}

