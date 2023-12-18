package com.nullpointer.nourseCompose.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.ui.viewModel.MeasureViewModel


@Composable
fun MeasureScreen(
    measureViewModel: MeasureViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Measure") })
        }
    ) {
        Box(modifier = Modifier.padding(it))
    }
}