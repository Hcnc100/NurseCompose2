package com.nullpointer.nourseCompose.state

import android.content.Context
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nullpointer.nourseCompose.models.data.MeasureError


@Stable
class MeasureScreenState(
    context: Context,
    snackbarHostState: SnackbarHostState,
    val lazyGridState: LazyGridState,
):SimpleScreenState(context, snackbarHostState){
    suspend fun showSnackMessage(measureError: MeasureError) {

        val titleMeasure = context.getString(measureError.titleMeasure)
        val message = context.getString(measureError.message, titleMeasure)
        showSnackMessage(message)
    }
}


@Composable
fun rememberMeasureScreenState(
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    lazyGridState: LazyGridState = rememberLazyGridState(),
) = remember(snackbarHostState, lazyGridState){
    MeasureScreenState(
        context = context,
        snackbarHostState = snackbarHostState,
        lazyGridState = lazyGridState,
    )
}
