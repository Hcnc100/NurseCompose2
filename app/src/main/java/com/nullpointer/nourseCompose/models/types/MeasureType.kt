package com.nullpointer.nourseCompose.models.types

import android.graphics.Color


enum class MeasureType(
    val maxValue1: Float? = null,
    val minValue1: Float? = null,
    val maxValue2: Float? = null,
    val minValue2: Float? = null,
    val suffix: String,
    val color1: Int,
    val color2: Int? = null
) {
    TEMPERATURE(
        maxValue1 = 37.0f,
        minValue1 = 36.5f,
        suffix = "°",
        color1 = Color.GREEN
    ),
    GLUCOSE(
        maxValue1 = 80.0f,
        minValue1 = 120.0f,
        suffix = "mg/dl",
        color1 = Color.MAGENTA
    ),
    OXYGEN(
        maxValue1 = 100.0f,
        minValue1 = 95.0f,
        suffix = "%",
        color1 = Color.BLUE
    ),
    PRESSURE(
        suffix = "mm Hg",
        // * range down
        minValue2 = 90.0f, // sistólica
        minValue1 = 60.0f, // diastólica

        // * range up
        maxValue1 = 130.0f, // sistólica
        maxValue2 = 80.0f, // diastólica
        color1 = Color.CYAN,
        color2 = Color.RED
    )

}