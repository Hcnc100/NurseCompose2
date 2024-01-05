package com.nullpointer.nourseCompose.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.models.data.SettingsData
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
            NumberMeasureRow(
                settingsData = settingsData,
                updateMeasureGraph = settingsViewModel::updateNumberMeasureGraph
            )
        }
    }
}

@Composable
fun AppbarSettings(
    actionBack: () -> Unit
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White,
        title = { Text(text = stringResource(R.string.title_settings)) },
        navigationIcon = {
            IconButton(
                onClick = actionBack,
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = null
                    )
                }
            )
        }
    )
}

@Composable
fun NumberMeasureRow(
    settingsData: SettingsData?,
    updateMeasureGraph: (Int) -> Unit
) {


    val items = listOf(10, 20, 30, 40, 50, 100)
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedValue by rememberSaveable(settingsData) {
        mutableIntStateOf(settingsData?.numberMeasureGraph ?: items[0])
    }

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(50.dp)
    ) {
        Text(
            text = stringResource(R.string.message_number_items_graph),
            modifier = Modifier.weight(3f),
            fontSize = 12.sp
        )

        Box(
            modifier = Modifier.weight(2f)
        ) {

            OutlinedTextField(
                value = selectedValue.toString(),
                onValueChange = {},
                enabled = false,
                modifier = Modifier.clickable { expanded = true },
                textStyle = TextStyle(
                    textAlign = TextAlign.End
                ),
                leadingIcon = {
                    val icon =
                        if (expanded) R.drawable.baseline_arrow_drop_up_24 else R.drawable.baseline_arrow_drop_down_24
                    Icon(imageVector = ImageVector.vectorResource(icon), contentDescription = null)
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                items.forEach { label ->
                    DropdownMenuItem(
                        onClick = {
                            selectedValue = label
                            expanded = false
                            updateMeasureGraph(label)
                        },
                    ) {
                        Text(text = label.toString())
                    }
                }
            }
        }
    }
}