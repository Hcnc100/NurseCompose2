package com.nullpointer.nourseCompose.models.data

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/** Presentation only: preserves the stored float's decimal representation. */
internal object MeasureValueFormatter {
    fun format(value: Float, second: Float?, paired: Boolean, unit: String, locale: Locale): String {
        val numbers = NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = false
            maximumFractionDigits = 340
        }
        fun number(value: Float): String = if (value.isFinite()) {
            numbers.format(BigDecimal(value.toString()))
        } else "—"
        val reading = if (paired) "${number(value)}/${second?.let(::number) ?: "—"}" else number(value)
        return "$reading $unit"
    }
}
