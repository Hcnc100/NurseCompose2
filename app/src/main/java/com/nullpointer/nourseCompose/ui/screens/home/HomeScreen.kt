package com.nullpointer.nourseCompose.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nullpointer.nourseCompose.R
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
import com.nullpointer.nourseCompose.ui.viewModel.SelectViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency


@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    destinationsNavigator: DestinationsNavigator,
    homeViewModel: HomeViewModel = hiltViewModel(),
    selectViewModel: SelectViewModel = hiltViewModel(),
    homeState: HomeState = rememberHomeState(
        selectExportDocumentSuccess = homeViewModel::exportMeasureDatabase,
        selectImportDocumentSuccess = homeViewModel::importMeasureDatabase
    )
) {

    val isLoading = homeViewModel.isLoading

    val (scaffoldState, navHostController) = homeState

    val currentDestination by navHostController.appCurrentDestinationAsState()
    val destination = HomeNavItems.values().find { it.destination == currentDestination }
    val (selectedDrawerActionDialog, changeSelectDrawerActions) = remember {
        mutableStateOf<DrawerActions?>(null)
    }
    val listSelected = selectViewModel.measureSelected

    BackHandler(
        enabled = listSelected.isNotEmpty() || selectedDrawerActionDialog != null || scaffoldState.drawerState.isOpen
    ) {
        when {
            listSelected.isNotEmpty() -> selectViewModel.clearSelection()
            selectedDrawerActionDialog != null -> changeSelectDrawerActions(null)
            scaffoldState.drawerState.isOpen -> homeState.closeDrawer()
        }
    }

    LaunchedEffect(key1 = Unit) {
        homeViewModel.message.collect(homeState::showSnackBar)
    }

    Scaffold(
        drawerGesturesEnabled = listSelected.isEmpty(),
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
                currentTitle = destination?.title,
                openDrawer = homeState::openDrawer,
                countSelected = listSelected.size,
                clearSelected = selectViewModel::clearSelection,
            )
        },
        bottomBar = {
            HomeBottomNavBar(
                navController = navHostController,
                currentDestination = currentDestination,
                actionClearSelected = selectViewModel::clearSelection,
            )
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DestinationsNavHost(
                navController = navHostController,
                navGraph = NavGraphs.homeGraph,
                modifier = Modifier.fillMaxSize(),
                dependenciesContainerBuilder = {
                    dependency(selectViewModel)
                }
            )

            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }


    when (selectedDrawerActionDialog) {
        IMPORT -> DrawerActionDialog(
            title = stringResource(id = selectedDrawerActionDialog.title),
            message = stringResource(R.string.message_import_data),
            closeDialog = {
                changeSelectDrawerActions(null)
                if (it) {
                    homeState.selectImportFile()
                }
            }
        )

        CLEAR_DATA -> DrawerActionDialog(
            title = stringResource(id = selectedDrawerActionDialog.title),
            message = stringResource(R.string.message_deleter_all_data),
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

