package com.nullpointer.nourseCompose.state

import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.nullpointer.nourseCompose.interfaces.RootDestinationActions
import com.ramcosta.composedestinations.navigation.navigate

@Stable
class RootState(
    private val scaffoldState: ScaffoldState,
    private val navHostController: NavHostController,
) {
    operator fun component1() = scaffoldState

    operator fun component2() = navHostController

    operator fun component3() = rootDestinationActions

    private val rootDestinationActions = RootDestinationActions {
        navHostController.navigate(it)
    }
}


@Composable
fun rememberRootState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navHostController: NavHostController = rememberNavController(),
) = remember(scaffoldState, navHostController) {
    RootState(
        scaffoldState = scaffoldState,
        navHostController = navHostController
    )
}