package com.nullpointer.nourseCompose.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.nullpointer.nourseCompose.ui.screens.settings.components.AppbarSettings
import com.nullpointer.nourseCompose.ui.screens.settings.components.NumberMeasureOption
import com.nullpointer.nourseCompose.ui.screens.settings.viewModel.SettingsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@RootNavGraph
@Destination
@Composable
fun SettingsScreen(
    destinationsNavigator: DestinationsNavigator,
    settingsViewModel: SettingsViewModel
) {

    val settingsData by settingsViewModel.settingsData.collectAsState()

    Scaffold(
        topBar = {
            AppbarSettings(destinationsNavigator::popBackStack)
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            NumberMeasureOption(
                settingsData = settingsData,
                updateMeasureGraph = settingsViewModel::updateNumberMeasureGraph
            )
        }
    }
}



