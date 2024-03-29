package com.nullpointer.nourseCompose.state

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Stable
open class SimpleScreenState(
    val context: Context,
    val scaffoldState: ScaffoldState
) {
    suspend fun showSnackMessage(@StringRes stringRes: Int) {
        scaffoldState.snackbarHostState.showSnackbar(
            context.getString(stringRes)
        )
    }

    suspend fun showSnackMessage(message:String) {
        scaffoldState.snackbarHostState.showSnackbar(message)
    }
}

@Composable
fun rememberSimpleScreenState(
    context: Context = LocalContext.current,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
) = remember(scaffoldState) {
    SimpleScreenState(context, scaffoldState)
}