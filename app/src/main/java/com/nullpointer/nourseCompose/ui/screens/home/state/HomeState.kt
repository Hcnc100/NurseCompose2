package com.nullpointer.nourseCompose.ui.screens.home.state

import android.content.Context
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.nullpointer.nourseCompose.contants.Constants.MEASURE_DATABASE_BACKUP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Stable
class HomeState(
    private val context: Context,
    private val scaffoldState: ScaffoldState,
    private val coroutineScope: CoroutineScope,
    private val navHostController: NavHostController,
) {

    operator fun component1() = scaffoldState
    operator fun component2() = navHostController


    fun showSnackBar(message: String) = coroutineScope.launch {
        scaffoldState.snackbarHostState.showSnackbar(message)
    }

    fun openDrawer() = coroutineScope.launch {
        scaffoldState.drawerState.open()
    }

    fun closeDrawer() = coroutineScope.launch {
        scaffoldState.drawerState.close()
    }

    fun createDatabaseBackUpFile(
        writeFile: (File) -> Unit
    ) {
        val csvFile = File(context.filesDir, MEASURE_DATABASE_BACKUP)
        writeFile(csvFile)
    }
}


@Composable
fun rememberHomeState(
    context: Context = LocalContext.current,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navHostController: NavHostController = rememberNavController()
) = remember(scaffoldState, coroutineScope) {
    HomeState(
        context = context,
        scaffoldState = scaffoldState,
        coroutineScope = coroutineScope,
        navHostController = navHostController
    )
}

