package com.nullpointer.nourseCompose.ui.screens.home.widgets

import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.nullpointer.nourseCompose.navigation.HomeNavItems
import com.nullpointer.nourseCompose.ui.screens.destinations.Destination
import com.ramcosta.composedestinations.navigation.navigate


@Composable
fun HomeBottomNavBar(
    navController: NavController,
    currentDestination: Destination?
) {

    BottomAppBar {
        HomeNavItems.values().map {
            BottomNavigationItem(
                alwaysShowLabel = false,
                label = { Text(text = it.title) },
                selected = it.destination == currentDestination,
                onClick = {
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
                        painter = painterResource(id = it.icon),
                        contentDescription = it.title
                    )
                }
            )
        }
    }

}