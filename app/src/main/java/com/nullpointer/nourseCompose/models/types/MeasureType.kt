package com.nullpointer.nourseCompose.models.types

import android.graphics.Color
import androidx.annotation.StringRes
import com.nullpointer.nourseCompose.R


enum class MeasureType(
    val maxValue1: Float? = null,
    val minValue1: Float? = null,
    val maxValue2: Float? = null,
    val minValue2: Float? = null,
    val suffix: String,
    val color1: Int,
    val color2: Int = color1,
    @StringRes
    val titleMeasure: Int
) {
    TEMPERATURE(
        maxValue1 = 37.0f,
        minValue1 = 36.0f,
        suffix = "°",
        color1 = Color.parseColor("#ff5722"),
        titleMeasure = R.string.title_temperature
    ),
    GLUCOSE(
        maxValue1 = 120.0f,
        minValue1 = 80.0f,
        suffix = "mg/dl",
        color1 = Color.parseColor("#673ab7"),
        titleMeasure = R.string.title_glucose
    ),
    OXYGEN(
        maxValue1 = 100.0f,
        minValue1 = 95.0f,
        suffix = "%",
        color1 = Color.parseColor("#03a9f4"),
        titleMeasure = R.string.title_oxygen
    ),
    PRESSURE(
        suffix = "mm Hg",

        minValue1 = 90.0f, // diastólica
        maxValue1 = 129.0f, // sistólica


        minValue2 = 60.0f, // sistólica
        maxValue2 = 84.0f, // diastólica
        color1 = Color.parseColor("#f44336"),
        color2 = Color.parseColor("#673ab7"),
        titleMeasure = R.string.title_pressure
    )

}