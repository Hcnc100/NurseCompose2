package com.nullpointer.nourseCompose.models.types

enum class MeasureType(
    val maxValue: Float? = null,
    val minValue: Float? = null,
    val prefix: String
) {
    TEMPERATURE(
        maxValue = 37.2f,
        minValue = 36.1f,
        prefix = "°"
    ),
    GLUCOSE(
        maxValue = 80.0f,
        minValue = 120.0f,
        prefix = "ml"
    ),
    OXYGEN(
        maxValue = 100.0f,
        minValue = 90.0f,
        prefix = "%"
    ),
    PRESSURE(
        prefix = ""
    )

}