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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.nullpointer.nourseCompose.models.data.MeasureData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeasureGraphList(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    measureList: LazyPagingItems<MeasureData>,
    graphHeader: @Composable () -> Unit
) {


    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            measureList.loadState.source.refresh is LoadState.Loading && measureList.itemCount == 0 -> CircularProgressIndicator()
            measureList.loadState.source.refresh is LoadState.NotLoading && measureList.itemCount == 0 -> Text(
                text = "Without Data"
            )

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
                    MeasureItem(
                        measureData = measureList[it]!!,
//                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }


}