package com.nullpointer.nourseCompose.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.ui.share.AddMeasureDialog
import com.nullpointer.nourseCompose.ui.share.MeasureGraph
import com.nullpointer.nourseCompose.ui.share.MeasureGraphList
import com.nullpointer.nourseCompose.ui.viewModel.MeasureViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun MeasureScreen(
    measureViewModel: MeasureViewModel = hiltViewModel(),
    lazyListState: LazyListState = rememberLazyListState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {

    val listTemperature = measureViewModel.listPagingTemperature.collectAsLazyPagingItems()
    val lastTemperatureList by measureViewModel.lastTemperatureList.collectAsState()

    var isVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Measure") })
        },
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
    ) {
        MeasureGraphList(
            measureList = listTemperature,
            lazyListState = lazyListState,
            modifier = Modifier.padding(it),
            graphHeader = {
                MeasureGraph(
                    measureList = lastTemperatureList,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

            }
        )

        if (isVisible) {
            AddMeasureDialog(
                measureType = MeasureType.TEMPERATURE,
                onDismissDialog = { value ->
                    isVisible = false
                    value?.let {
                        measureViewModel.addTemperatureData(value).invokeOnCompletion {
                            coroutineScope.launch {
                                delay(200)
                                lazyListState.scrollToItem(0)
                            }
                        }
                    }
                }
            )
        }
    }
}

