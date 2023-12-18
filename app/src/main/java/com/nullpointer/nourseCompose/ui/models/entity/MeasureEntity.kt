package com.nullpointer.nourseCompose.ui.models.entity

import com.nullpointer.nourseCompose.ui.models.types.MeasureType

data class MeasureEntity(
    val id: Int = 0,
    val createAt: Double,
    val type: MeasureType,
    val dateInMillis: Long = System.currentTimeMillis(),
)