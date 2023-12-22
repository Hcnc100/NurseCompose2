package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.nullpointer.nourseCompose.R

@Composable
fun HomeTopAppbar(
    openDrawer: () -> Unit,
    currentTitle: String
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White,
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_dehaze_24),
                    contentDescription = null
                )
            }
        },
        title = { Text(text = currentTitle) }
    )
}