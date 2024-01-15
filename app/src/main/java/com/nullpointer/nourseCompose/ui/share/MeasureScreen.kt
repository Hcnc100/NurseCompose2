package com.nullpointer.nourseCompose.ui.share

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.ui.share.addMeasureDialog.AddMeasureDialog
import com.nullpointer.nourseCompose.ui.share.graph.MeasureGraph
import com.nullpointer.nourseCompose.ui.share.measureGraphList.MeasureGraphList


@Composable
fun MeasureScreen(
    measureType: MeasureType,
    isSelectedEnable: Boolean,
    lazyGridState: LazyGridState,
    scaffoldState: ScaffoldState,
    lastMeasureList: List<MeasureData>,
    deleterMeasureSelected: () -> Unit,
    listMeasureSelected: Map<Int, MeasureData>,
    addMeasureSelected: (MeasureData) -> Unit,
    pagingListMeasure: LazyPagingItems<MeasureData>,
    addMeasureData: (value1: Float, value2: Float?) -> Unit,
) {


    var isVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            MeasureFAB(
                showDialogAdd = { isVisible = true },
                isSelectedEnable = isSelectedEnable,
                deleterMeasureSelected = deleterMeasureSelected,
            )
        }
    ) { paddingValues ->
        MeasureGraphList(
            measureType = measureType,
            listMeasureSelected = listMeasureSelected,
            isSelectedEnable = isSelectedEnable,
            addMeasureSelected = addMeasureSelected,
            measureList = pagingListMeasure,
            lazyGridState = lazyGridState,
            modifier = Modifier.padding(paddingValues)
        ) {
            MeasureGraph(
                measureType = measureType,
                measureList = lastMeasureList,
                modifier = Modifier.fillMaxSize()
            )

        }

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


