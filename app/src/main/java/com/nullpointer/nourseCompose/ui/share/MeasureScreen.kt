package com.nullpointer.nourseCompose.ui.share

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType


@Composable
fun MeasureScreen(
    measureType: MeasureType,
    lazyListState: LazyListState,
    scaffoldState: ScaffoldState,
    lastMeasureList: List<MeasureData>,
    addMeasureData: (value1: Float, value2: Float?) -> Unit,
    pagingListMeasure: LazyPagingItems<MeasureData>,
) {


    var isVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                isVisible = true

            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = null,
                )
            }
        }
    ) { paddingValues ->
        MeasureGraphList(
            measureList = pagingListMeasure,
            lazyListState = lazyListState,
            modifier = Modifier.padding(paddingValues),
            graphHeader = {
                MeasureGraph(
                    measureType = measureType,
                    measureList = lastMeasureList,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

            }
        )

        if (isVisible) {
            AddMeasureDialog(
                measureType = measureType,
                onDismissDialog = { value1, value2 ->
                    isVisible = false
                    value1?.let { addMeasureData(value1, value2) }
                }
            )
        }
    }
}

