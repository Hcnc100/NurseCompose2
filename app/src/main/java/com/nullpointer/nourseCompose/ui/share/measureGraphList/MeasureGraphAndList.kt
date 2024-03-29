package com.nullpointer.nourseCompose.ui.share.measureGraphList

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType

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
                        MeasureGraphAndListPortrait(
                            isSelectedEnable = isSelectedEnable,
                            lazyGridState = lazyGridState,
                            measureList = measureList,
                            listMeasureSelected = listMeasureSelected,
                            addMeasureSelected = addMeasureSelected,
                            graphHeader = graphHeader
                        )
                    }

                    else -> {
                        MeasureGraphAndListLandscape(
                            isSelectedEnable = isSelectedEnable,
                            lazyGridState = lazyGridState,
                            measureList = measureList,
                            listMeasureSelected = listMeasureSelected,
                            addMeasureSelected = addMeasureSelected,
                            graphHeader = graphHeader
                        )
                    }
                }
            }

        }
    }
}