package com.nullpointer.nourseCompose.ui.models.data

import com.nullpointer.nourseCompose.ui.models.types.MeasureType

data class MeasureData(
    val int: Long,
    val value: Double,
    val createAt: Long,
    val type: MeasureType,
)
