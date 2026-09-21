package com.nullpointer.nourseCompose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.nullpointer.nourseCompose.interfaces.RootDestinationActions
import com.ramcosta.composedestinations.navigation.navigate

@Stable
class RootState(
    private val navHostController: NavHostController,
) {
    operator fun component1() = navHostController
    operator fun component2() = rootDestinationActions

    operator fun component3() = rootDestinationActions

    private val rootDestinationActions = RootDestinationActions {
        navHostController.navigate(it)
    }
}


@Composable
fun rememberRootState(
    navHostController: NavHostController = rememberNavController(),
) = remember(navHostController) {
    RootState(
        navHostController = navHostController
    )
}
