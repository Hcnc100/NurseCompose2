package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.nullpointer.nourseCompose.R

@Composable
fun HomeTopAppbar(
    countSelected: Int,
    @StringRes
    currentTitle: Int?,
    openDrawer: () -> Unit,
    clearSelected: () -> Unit
) {

    val menuIcon = @Composable { getNavigationIcon(openDrawer) }

    val backgroundColor by animateColorAsState(
        if (countSelected == 0) MaterialTheme.colors.primary else MaterialTheme.colors.secondary,
        label = "ANIMATION_CHANGE_COLOR_TOOLBAR",
        animationSpec = tween(durationMillis = 300)
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
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_dehaze_24),
            contentDescription = null
        )
    }
}

@Composable
fun getClearIcon(clearSelected: () -> Unit) {
    IconButton(onClick = clearSelected) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_clear_24),
            contentDescription = null
        )
    }
}


@Composable
fun getAppBarTitle(
    countSelected: Int,
    @StringRes
    currentTitle: Int?,
): String {
    return when (countSelected) {
        0 -> currentTitle?.let { stringResource(id = it) } ?: ""
        else -> stringResource(R.string.title_selected_measure, countSelected)
    }
}