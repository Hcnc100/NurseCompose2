package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.nullpointer.nourseCompose.navigation.HomeNavItems
import com.nullpointer.nourseCompose.ui.screens.destinations.Destination
import com.ramcosta.composedestinations.navigation.navigate


@Composable
fun HomeBottomNavBar(
    navController: NavController,
    currentDestination: Destination?,
    actionClearSelected: () -> Unit
) {

    BottomAppBar(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White
    ) {
        HomeNavItems.values().map {
            BottomNavigationItem(
                alwaysShowLabel = false,
                label = { Text(text = stringResource(id = it.title)) },
                selected = it.destination == currentDestination,
                onClick = {
                    if(it.destination != currentDestination) {
                        actionClearSelected()
                    }
                    navController.navigate(it.destination) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = it.icon),
                        contentDescription = stringResource(id = it.title)
                    )
                }
            )
        }
    }

}