package com.nullpointer.nourseCompose.ui.screens.home.state

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.annotation.StringRes
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
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
    private val scaffoldState: ScaffoldState,
    private val coroutineScope: CoroutineScope,
    private val navHostController: NavHostController,
    private val selectExportDocumentResult: ManagedActivityResultLauncher<String, Uri?>,
    private val selectImportDocumentResult: ManagedActivityResultLauncher<String, Uri?>,
) {
    operator fun component1() = scaffoldState
    operator fun component2() = navHostController


    fun showSnackBar(message: String) = coroutineScope.launch {
        scaffoldState.snackbarHostState.showSnackbar(message)
    }

    fun showSnackBar(
        @StringRes
        message: Int,
    ) = coroutineScope.launch {
        scaffoldState.snackbarHostState.showSnackbar(
            context.getString(message)
        )
    }


    fun openDrawer() = coroutineScope.launch {
        scaffoldState.drawerState.open()
    }

    fun closeDrawer() = coroutineScope.launch {
        scaffoldState.drawerState.close()
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
    scaffoldState: ScaffoldState = rememberScaffoldState(),
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
) = remember(scaffoldState, coroutineScope, selectExportDocumentResult) {
    HomeState(
        context = context,
        scaffoldState = scaffoldState,
        coroutineScope = coroutineScope,
        navHostController = navHostController,
        selectExportDocumentResult = selectExportDocumentResult,
        selectImportDocumentResult = selectImportDocumentResult,
    )
}

