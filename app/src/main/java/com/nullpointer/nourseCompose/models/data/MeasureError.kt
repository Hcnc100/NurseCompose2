package com.nullpointer.nourseCompose.models.data

import androidx.annotation.StringRes

data class MeasureError(
    @StringRes
    val message: Int,
    @StringRes
    val titleMeasure: Int
)
