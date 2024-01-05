package com.nullpointer.nourseCompose.ui.share

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeasureGraphList(
    measureType: MeasureType,
    isSelectedEnable: Boolean,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    measureList: LazyPagingItems<MeasureData>,
    addMeasureSelected: (MeasureData) -> Unit,
    listMeasureSelected: SnapshotStateMap<Int, MeasureData>,
    graphHeader: @Composable () -> Unit
) {



    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            measureList.loadState.source.refresh is LoadState.Loading && measureList.itemCount == 0 -> CircularProgressIndicator()
            measureList.loadState.source.refresh is LoadState.NotLoading && measureList.itemCount == 0 -> {
                val titleMeasure = stringResource(measureType.titleMeasure)
                val message = stringResource(R.string.message_empty_measure, titleMeasure)
                Text(
                    text = message
                )
            }

            else -> LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                stickyHeader {
                    HeaderMeasureList(graphHeader)
                }

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
    }


}