package com.nullpointer.nourseCompose.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.nullpointer.nourseCompose.ui.screens.destinations.SettingsScreenDestination
import com.nullpointer.nourseCompose.ui.screens.home.actions.DrawerActions
import com.nullpointer.nourseCompose.ui.screens.home.actions.DrawerActions.CLEAR_DATA
import com.nullpointer.nourseCompose.ui.screens.home.actions.DrawerActions.EXPORT
import com.nullpointer.nourseCompose.ui.screens.home.actions.DrawerActions.IMPORT
import com.nullpointer.nourseCompose.ui.screens.home.actions.DrawerActions.SETTINGS
import com.nullpointer.nourseCompose.ui.screens.home.state.HomeState
import com.nullpointer.nourseCompose.ui.screens.home.state.rememberHomeState
import com.nullpointer.nourseCompose.ui.screens.home.viewModel.HomeViewModel
import com.nullpointer.nourseCompose.ui.screens.home.widgets.DrawerContent
import com.nullpointer.nourseCompose.ui.screens.home.widgets.HomeBottomNavBar
import com.nullpointer.nourseCompose.ui.screens.home.widgets.HomeTopAppbar
import com.nullpointer.nourseCompose.ui.screens.home.widgets.dialogs.DrawerActionDialog
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency


@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator,
    homeState: HomeState = rememberHomeState(
        selectExportDocumentSuccess = homeViewModel::exportMeasureDatabase,
        selectImportDocumentSuccess = homeViewModel::importMeasureDatabase
    ),
) {

    val (scaffoldState, navHostController, selectedState) = homeState

    val currentDestination by navHostController.appCurrentDestinationAsState()
    val destination = HomeNavItems.values().find { it.destination == currentDestination }
    val (selectedDrawerActionDialog, changeSelectDrawerActions) = remember {
        mutableStateOf<DrawerActions?>(null)
    }

    BackHandler(
        enabled = selectedState.currentValueSelected != 0
    ) {
        selectedState.clearNumberSelected()
    }

    LaunchedEffect(key1 = Unit) {
        homeViewModel.message.collect(homeState::showSnackBar)
    }

    Scaffold(
        drawerGesturesEnabled = homeState.currentValueSelected == 0,
        scaffoldState = scaffoldState,
        drawerShape = customShape(),
        drawerContent = {
            DrawerContent(
                drawerAction = { drawerAction ->
                    homeState.closeDrawer()
                    when (drawerAction) {
                        EXPORT -> homeState.selectExportFile()
                        SETTINGS -> destinationsNavigator.navigate(SettingsScreenDestination)
                        else -> changeSelectDrawerActions(drawerAction)
                    }
                }
            )
        },
        topBar = {
            HomeTopAppbar(
                currentTitle = destination?.title.orEmpty(),
                openDrawer = homeState::openDrawer,
                countSelected = homeState.currentValueSelected,
                clearSelected = homeState::clearNumberSelected
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
            dependenciesContainerBuilder = {
                dependency(selectedState)
            }
        )
    }


    when (selectedDrawerActionDialog) {
        IMPORT -> DrawerActionDialog(
            title = selectedDrawerActionDialog.title,
            message = "When importing information, saved data will be deleted. Are you sure?",
            closeDialog = {
                changeSelectDrawerActions(null)
                if (it) {
                    homeState.selectImportFile()
                }
            }
        )

        CLEAR_DATA -> DrawerActionDialog(
            title = selectedDrawerActionDialog.title,
            message = "Do you want to delete all saved data?",
            closeDialog = {
                changeSelectDrawerActions(null)
                if (it) {
                    homeViewModel.deleterAllData()
                }
            }
        )

        else -> Unit
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

