package com.nullpointer.nourseCompose.ui.share

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.ui.share.measureItem.MeasureItem

@Composable
fun MeasureGraphList(
    measureType: MeasureType,
    isSelectedEnable: Boolean,
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
    measureList: LazyPagingItems<MeasureData>,
    addMeasureSelected: (MeasureData) -> Unit,
    listMeasureSelected: Map<Int, MeasureData>,
    orientation: Int = LocalConfiguration.current.orientation,
    graphHeader: @Composable () -> Unit,

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

            else -> {

                when (orientation) {
                    ORIENTATION_PORTRAIT -> {
                        Column {
                            graphHeader()
                            LazyVerticalGrid(
                                state = lazyGridState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                columns = GridCells.Adaptive(150.dp),
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

                    }

                    else -> {
                        Row {
                            Box(
                                modifier = Modifier
                                    .weight(0.5F)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center,
                            ) { graphHeader() }
                            LazyVerticalGrid(
                                state = lazyGridState,
                                modifier = Modifier.weight(0.5F),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                columns = GridCells.Adaptive(150.dp),
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
                    }
                }
            }

        }
    }
}