package com.nullpointer.nourseCompose.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.navigation.HomeNavItems
import com.nullpointer.nourseCompose.ui.screens.NavGraphs
import com.nullpointer.nourseCompose.ui.screens.appCurrentDestinationAsState
import com.nullpointer.nourseCompose.ui.screens.home.widgets.HomeBottomNavBar
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen() {

    val navHostController = rememberNavController()
    val currentDestination by navHostController.appCurrentDestinationAsState()
    val destination = HomeNavItems.values().find { it.destination == currentDestination }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerShape = customShape(),
        drawerContent = {
            Column {
                ListItem(
                    modifier = Modifier.clickable {

                    },
                    text = { Text("Settings") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_build_24),
                            contentDescription = null
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable {

                    },
                    text = { Text("Export") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_import_export_24),
                            contentDescription = null
                        )
                    }
                )
                ListItem(
                    modifier = Modifier.clickable {

                    },
                    text = { Text("Import") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_import_export_24),
                            contentDescription = null
                        )
                    }
                )
            }
        },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_dehaze_24),
                            contentDescription = null
                        )
                    }
                },
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

