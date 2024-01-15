package com.nullpointer.nourseCompose.ui.share.measureGraphList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.ui.preview.config.LandscapePreview
import com.nullpointer.nourseCompose.ui.share.graph.MeasureGraph
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MeasureGraphAndListLandscape(
    isSelectedEnable: Boolean,
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
    measureList: LazyPagingItems<MeasureData>,
    listMeasureSelected: Map<Int, MeasureData>,
    addMeasureSelected: (MeasureData) -> Unit,
    graphHeader: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.weight(0.5F),
        ) { graphHeader() }
        MeasureGridList(
            isSelectedEnable = isSelectedEnable,
            lazyGridState = lazyGridState,
            measureList = measureList,
            listMeasureSelected = listMeasureSelected,
            addMeasureSelected = addMeasureSelected,
            modifier = Modifier.weight(0.5F)
        )
    }
}

@LandscapePreview
@Composable
private fun MeasureGraphAndListLandscapePreview() {
    val pagingData = PagingData.from(MeasureData.listMeasureExample)
    val fakeDataFlow = MutableStateFlow(pagingData).collectAsLazyPagingItems()

    MeasureGraphAndListLandscape(
        addMeasureSelected = {},
        graphHeader = {
            MeasureGraph(
                measureType = MeasureData.listMeasureExample.first().type,
                measureList = MeasureData.listMeasureExample,
                modifier = Modifier.fillMaxSize()
            )
        },
        measureList = fakeDataFlow,
        listMeasureSelected = emptyMap(),
        isSelectedEnable = true,
        lazyGridState = LazyGridState(),
    )
}