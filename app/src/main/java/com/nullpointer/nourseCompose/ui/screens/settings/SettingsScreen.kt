package com.nullpointer.nourseCompose.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nullpointer.nourseCompose.ui.screens.settings.components.AppbarSettings
import com.nullpointer.nourseCompose.ui.screens.settings.components.NumberMeasureOption
import com.nullpointer.nourseCompose.ui.screens.settings.viewModel.SettingsViewModel
import com.nullpointer.nourseCompose.ui.screens.destinations.DiagnosticsScreenDestination
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
        Column(modifier = Modifier.padding(it).padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())) {
            NumberMeasureOption(
                settingsData = settingsData,
                updateMeasureGraph = settingsViewModel::updateNumberMeasureGraph
            )
            androidx.compose.material3.Button(shape = androidx.compose.material3.MaterialTheme.shapes.medium, onClick = { destinationsNavigator.navigate(DiagnosticsScreenDestination) }) {
                androidx.compose.material3.Text(stringResource(com.nullpointer.nourseCompose.R.string.title_diagnostics))
            }
        }
    }
}



