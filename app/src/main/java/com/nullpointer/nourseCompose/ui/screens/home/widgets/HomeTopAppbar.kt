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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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

    val menuIcon = @Composable { getNavigationIcon(openDrawer) }

    val backgroundColor by animateColorAsState(
        if (countSelected == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        label = "ANIMATION_CHANGE_COLOR_TOOLBAR",
        animationSpec = tween(durationMillis = 300)
    )

    TopAppBar(
        windowInsets = WindowInsets.statusBars,
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White),
        navigationIcon = if (countSelected == 0) menuIcon else { {} },
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
