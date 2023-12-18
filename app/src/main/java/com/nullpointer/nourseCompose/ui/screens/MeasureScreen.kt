package com.nullpointer.nourseCompose.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.share.MeasureGraphList
import com.nullpointer.nourseCompose.ui.viewModel.MeasureViewModel


@Composable
fun MeasureScreen(
    measureViewModel: MeasureViewModel = hiltViewModel(),
    lazyListState: LazyListState = rememberLazyListState()
) {

    val listTemperature by measureViewModel.listTemperature.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Measure") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { measureViewModel.addFakeData() }) {
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
        )
    }
}

