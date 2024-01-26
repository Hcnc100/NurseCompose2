package com.nullpointer.nourseCompose.ui.share.measureGraphList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.ui.preview.config.SimplePreview
import com.nullpointer.nourseCompose.ui.share.graph.MeasureGraph
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun MeasureGraphAndListPortrait(
    isSelectedEnable: Boolean,
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
    measureList: LazyPagingItems<MeasureData>,
    listMeasureSelected: Map<Int, MeasureData>,
    addMeasureSelected: (MeasureData) -> Unit,
    graphHeader: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            graphHeader()
        }
        MeasureGridList(
            isSelectedEnable = isSelectedEnable,
            lazyGridState = lazyGridState,
            measureList = measureList,
            listMeasureSelected = listMeasureSelected,
            addMeasureSelected = addMeasureSelected,
        )
    }
}

@SimplePreview
@Composable
private fun MeasureGraphListAndGraphPortraitPreview() {

    val pagingData = PagingData.from(MeasureData.listMeasureExample)
    val fakeDataFlow = MutableStateFlow(pagingData).collectAsLazyPagingItems()


    MeasureGraphAndListPortrait(
        addMeasureSelected = {},
        graphHeader = {
            MeasureGraph(
                measureType = MeasureData.listMeasureExample.first().type,
                measureList = MeasureData.listMeasureExample,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        },
        isSelectedEnable = false,
        measureList = fakeDataFlow,
        listMeasureSelected = emptyMap(),
        lazyGridState = LazyGridState(),

        )
}