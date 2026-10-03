package com.nullpointer.nourseCompose.domain.medication

import java.math.BigDecimal
import java.math.RoundingMode

/** UI units only; persistence and scheduling continue to use whole minutes. */
enum class ReminderIntervalUnit(val minutesPerUnit: Int) {
    MINUTES(1), HOURS(60);

    fun toMinutes(text: String): Int? = runCatching {
        val value = text.replace(',', '.').toBigDecimal()
        require(value > BigDecimal.ZERO)
        if (this == MINUTES) require(value.stripTrailingZeros().scale() <= 0)
        value.multiply(minutesPerUnit.toBigDecimal()).setScale(0, RoundingMode.HALF_UP)
            .intValueExact().takeIf { it > 0 }
    }.getOrNull()

    fun format(minutes: Int): String = minutes.toBigDecimal()
        .divide(minutesPerUnit.toBigDecimal(), 2, RoundingMode.HALF_UP)
        .stripTrailingZeros().toPlainString()

    companion object {
        fun forMinutes(minutes: Int) = if (minutes >= 60 && minutes % 60 == 0) HOURS else MINUTES
    }
}
