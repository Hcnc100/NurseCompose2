package com.nullpointer.nourseCompose.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.nullpointer.nourseCompose.navigation.HomeNavItems
import com.nullpointer.nourseCompose.ui.screens.NavGraphs
import com.nullpointer.nourseCompose.ui.screens.appCurrentDestinationAsState
import com.nullpointer.nourseCompose.ui.screens.home.widgets.HomeBottomNavBar
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph


@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen() {

    val navHostController = rememberNavController()
    val currentDestination by navHostController.appCurrentDestinationAsState()

    val destination = HomeNavItems.values().find { it.destination == currentDestination }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = destination?.title.orEmpty()) }
            )
        },
        bottomBar = {
            HomeBottomNavBar(
                navController = navHostController,
                currentDestination = currentDestination,
            )
        }
    ) {
        DestinationsNavHost(
            navController = navHostController,
            navGraph = NavGraphs.homeGraph,
            modifier = Modifier.padding(it),
        )
    }
}


