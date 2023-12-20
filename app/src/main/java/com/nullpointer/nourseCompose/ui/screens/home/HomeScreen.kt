package com.nullpointer.nourseCompose.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.nourseCompose.navigation.HomeNavItems
import com.nullpointer.nourseCompose.ui.screens.NavGraphs
import com.nullpointer.nourseCompose.ui.screens.appCurrentDestinationAsState
import com.nullpointer.nourseCompose.ui.screens.home.state.HomeState
import com.nullpointer.nourseCompose.ui.screens.home.state.rememberHomeState
import com.nullpointer.nourseCompose.ui.screens.home.viewModel.HomeViewModel
import com.nullpointer.nourseCompose.ui.screens.home.widgets.DrawerContent
import com.nullpointer.nourseCompose.ui.screens.home.widgets.HomeBottomNavBar
import com.nullpointer.nourseCompose.ui.screens.home.widgets.HomeTopAppbar
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph


@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    homeState: HomeState = rememberHomeState(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {

    val (scaffoldState, navHostController) = homeState

    val currentDestination by navHostController.appCurrentDestinationAsState()
    val destination = HomeNavItems.values().find { it.destination == currentDestination }

    LaunchedEffect(key1 = Unit) {
        homeViewModel.message.collect(homeState::showSnackBar)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        drawerShape = customShape(),
        drawerContent = {
            DrawerContent(
                exportDatabase = {
                    homeState.createDatabaseBackUpFile { file ->
                        homeViewModel.exportMeasureDatabase(file)
                    }
                },
                importDatabase = {
                    homeState.createDatabaseBackUpFile { file ->
                        homeViewModel.importMeasureDatabase(file)
                    }
                }
            )
        },
        topBar = {
            HomeTopAppbar(
                currentTitle = destination?.title.orEmpty(),
                openDrawer = homeState::openDrawer
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


fun customShape() = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = (size.width * 0.75).toFloat(),
                bottom = size.height
            )
        )
    }
}

