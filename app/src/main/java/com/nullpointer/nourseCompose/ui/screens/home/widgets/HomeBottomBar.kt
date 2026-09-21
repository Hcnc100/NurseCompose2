package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.Modifier
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.nullpointer.nourseCompose.navigation.HomeNavItems
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.ui.screens.destinations.Destination
import com.ramcosta.composedestinations.navigation.navigate


@Composable
fun HomeBottomNavBar(
    navController: NavController,
    currentDestination: Destination?,
    actionClearSelected: () -> Unit
) {

    NavigationBar(
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
            HomeNavItems.entries.forEach { it ->
                NavigationBarItem(
                    label = { Text(text = stringResource(id = it.title)) },
                    selected = it.destination == currentDestination,
                    onClick = {
                        if (it.destination != currentDestination) actionClearSelected()
                        navController.navigate(it.destination) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(ImageVector.vectorResource(id = it.icon), stringResource(id = it.title)) }
                )
            }
    }
}
