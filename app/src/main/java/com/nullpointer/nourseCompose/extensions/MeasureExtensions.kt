package com.nullpointer.nourseCompose.extensions

import android.content.Context
import androidx.compose.material.SnackbarHostState
import com.nullpointer.nourseCompose.models.data.MeasureError

suspend fun SnackbarHostState.showSnackbar(measureError: MeasureError, context: Context) {
    val titleMeasure = context.getString(measureError.titleMeasure)
    val message = context.getString(measureError.message, titleMeasure)
    showSnackbar(message)
}
