package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.nullpointer.nourseCompose.R

@Composable
fun HomeTopAppbar(
    countSelected: Int,
    currentTitle: String,
    openDrawer: () -> Unit,
    clearSelected: () -> Unit
) {

    val menuIcon = @Composable { getNavigationIcon(openDrawer) }

    val backgroundColor: Color by animateColorAsState(
        if (countSelected == 0) MaterialTheme.colors.primary else MaterialTheme.colors.secondary,
        label = ""
    )

    TopAppBar(
        backgroundColor = backgroundColor,
        contentColor = Color.White,
        navigationIcon = if (countSelected == 0) menuIcon else null,
        title = { Text(text = getAppBarTitle(countSelected, currentTitle)) },
        actions = {
            if (countSelected != 0) {
                getClearIcon(clearSelected)
            }
        }
    )
}

@Composable
fun getNavigationIcon(openDrawer: () -> Unit) {
    IconButton(onClick = openDrawer) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_dehaze_24),
            contentDescription = null
        )
    }
}

@Composable
fun getClearIcon(clearSelected: () -> Unit) {
    IconButton(onClick = clearSelected) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_clear_24),
            contentDescription = null
        )
    }
}


@Composable
fun getAppBarTitle(countSelected: Int, currentTitle: String): String {
    return when (countSelected) {
        0 -> currentTitle
        else -> "Selected (${countSelected})"
    }
}