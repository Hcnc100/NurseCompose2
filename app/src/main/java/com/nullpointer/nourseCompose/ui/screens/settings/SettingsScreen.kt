package com.nullpointer.nourseCompose.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.nullpointer.nourseCompose.R
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@RootNavGraph
@Destination
@Composable
fun SettingsScreen(
    destinationsNavigator: DestinationsNavigator
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = destinationsNavigator::popBackStack,
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
    ) {
        Box(modifier = Modifier.padding(it))
    }
}