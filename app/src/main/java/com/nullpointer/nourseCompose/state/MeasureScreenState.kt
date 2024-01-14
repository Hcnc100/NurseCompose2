package com.nullpointer.nourseCompose.state

import android.content.Context
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nullpointer.nourseCompose.models.data.MeasureError


@Stable
class MeasureScreenState(
    context: Context,
    scaffoldState: ScaffoldState,
    val lazyGridState: LazyGridState,
):SimpleScreenState(context, scaffoldState){
    suspend fun showSnackMessage(measureError: MeasureError) {

        val titleMeasure = context.getString(measureError.titleMeasure)
        val message = context.getString(measureError.message, titleMeasure)
        showSnackMessage(message)
    }
}


@Composable
fun rememberMeasureScreenState(
    context: Context = LocalContext.current,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    lazyGridState: LazyGridState = rememberLazyGridState(),
) = remember(scaffoldState, lazyGridState){
    MeasureScreenState(
        context = context,
        scaffoldState = scaffoldState,
        lazyGridState = lazyGridState,
    )
}