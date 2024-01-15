package com.nullpointer.nourseCompose.ui.share.measureGraphList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.ui.preview.config.SmartPhonePreview
import com.nullpointer.nourseCompose.ui.share.measureItem.MeasureItem
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MeasureGridList(
    isSelectedEnable: Boolean,
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
    measureList: LazyPagingItems<MeasureData>,
    listMeasureSelected: Map<Int, MeasureData>,
    addMeasureSelected: (MeasureData) -> Unit,

    ) {
    LazyVerticalGrid(
        state = lazyGridState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        columns = GridCells.Adaptive(175.dp),
        contentPadding = PaddingValues(10.dp)
    ) {
        items(
            measureList.itemCount,
            key = measureList.itemKey { it.id },
            contentType = measureList.itemContentType { it.type },
        ) {
            // * when no use place holders
            // * no need check if the item is null

            val measureData = measureList[it]!!

            MeasureItem(
                isSelectedEnable = isSelectedEnable,
                addMeasureSelected = addMeasureSelected,
                measureData = measureData,
                isSelected = listMeasureSelected.containsKey(measureData.id)
//                        modifier = Modifier.animateItemPlacement(),
            )
        }
    }
}

@SmartPhonePreview
@Composable
fun MeasureGridListPreview() {
    val pagingData = PagingData.from(MeasureData.listMeasureExample)
    val fakeDataFlow = MutableStateFlow(pagingData).collectAsLazyPagingItems()
    MeasureGridList(
        addMeasureSelected = {},
        isSelectedEnable = false,
        measureList = fakeDataFlow,
        listMeasureSelected = emptyMap(),
        lazyGridState = LazyGridState(),
    )
}