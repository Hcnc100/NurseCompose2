package com.nullpointer.nourseCompose.ui.models.types

enum class MeasureType(
    val maxValue: Double? = null,
    val minValue: Double? = null,
    val prefix: String
) {
    TEMPERATURE(
        maxValue = 38.0,
        minValue = 34.0,
        prefix = "°"
    ),
    GLUCOSE(
        maxValue = 80.0,
        minValue = 120.0,
        prefix = "ml"
    ),
    OXYGEN(
        maxValue = 100.0,
        minValue = 90.0,
        prefix = "%"
    ),
    PRESSURE(
        prefix = ""
    )

}