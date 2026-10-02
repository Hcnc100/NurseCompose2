package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.nullpointer.nourseCompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppbar(
    countSelected: Int,
    @StringRes
    currentTitle: Int?,
    openDrawer: () -> Unit,
    clearSelected: () -> Unit
) {

    val isSelectionMode = countSelected != 0
    val backgroundColor by animateColorAsState(
        if (!isSelectionMode) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primaryContainer,
        label = "ANIMATION_CHANGE_COLOR_TOOLBAR",
        animationSpec = tween(durationMillis = 300)
    )
    val contentColor = if (!isSelectionMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer

    TopAppBar(
        windowInsets = WindowInsets.statusBars,
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        // Keep the drawer reachable even when measurements are selected.
        navigationIcon = { getNavigationIcon(openDrawer) },
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
            contentDescription = stringResource(R.string.action_open_menu),
            tint = LocalContentColor.current,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun getClearIcon(clearSelected: () -> Unit) {
    IconButton(onClick = clearSelected) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_clear_24),
            contentDescription = stringResource(R.string.action_clear_selection)
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
