package com.nullpointer.nourseCompose.ui.screens.home.state

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.annotation.StringRes
import androidx.compose.material3.DrawerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.nullpointer.nourseCompose.constants.Constants.EXTENSION_FILE
import com.nullpointer.nourseCompose.constants.Constants.MEASURE_DATABASE_BACKUP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

@Stable
class HomeState(
    private val context: Context,
    private val drawerState: DrawerState,
    val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope,
    private val navHostController: NavHostController,
    private val selectExportDocumentResult: ManagedActivityResultLauncher<String, Uri?>,
    private val selectImportDocumentResult: ManagedActivityResultLauncher<String, Uri?>,
) {
    operator fun component1() = drawerState
    operator fun component2() = navHostController


    fun showSnackBar(message: String) = coroutineScope.launch {
        snackbarHostState.showSnackbar(message)
    }

    fun showSnackBar(
        @StringRes
        message: Int,
    ) = coroutineScope.launch {
        snackbarHostState.showSnackbar(
            context.getString(message)
        )
    }


    fun openDrawer() = coroutineScope.launch {
        drawerState.open()
    }

    fun closeDrawer() = coroutineScope.launch {
        drawerState.close()
    }


    fun selectExportFile() {
        selectExportDocumentResult.launch("${MEASURE_DATABASE_BACKUP}_${System.currentTimeMillis()}.${EXTENSION_FILE}")
    }

    fun selectImportFile() {
        selectImportDocumentResult.launch("*/*")
    }


}


@Composable
fun rememberHomeState(
    context: Context = LocalContext.current,
    selectExportDocumentSuccess: (OutputStream) -> Unit,
    selectImportDocumentSuccess: (InputStream) -> Unit,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navHostController: NavHostController = rememberNavController(),
    @SuppressLint("Recycle") selectExportDocumentResult: ManagedActivityResultLauncher<String, Uri?> = rememberLauncherForActivityResult(
        CreateDocument("*/*")
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let(selectExportDocumentSuccess)
        }
    },
    @SuppressLint("Recycle") selectImportDocumentResult: ManagedActivityResultLauncher<String, Uri?> = rememberLauncherForActivityResult(
        GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.let(selectImportDocumentSuccess)
            }
        }
    ),
) = remember(drawerState, snackbarHostState, coroutineScope, selectExportDocumentResult) {
    HomeState(
        context = context,
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
        coroutineScope = coroutineScope,
        navHostController = navHostController,
        selectExportDocumentResult = selectExportDocumentResult,
        selectImportDocumentResult = selectImportDocumentResult,
    )
}

