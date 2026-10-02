package com.nullpointer.nourseCompose.ui.share.measureItem

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nullpointer.nourseCompose.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TimeMeasureIndicator(
    createAt: Long,
    context: Context = LocalContext.current
) {


    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = androidx.core.os.ConfigurationCompat.getLocales(configuration)[0]
        ?: java.util.Locale.getDefault()
    val use24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val dateString = remember(createAt, locale, use24Hour) {
        val timePattern = android.text.format.DateFormat.getBestDateTimePattern(
            locale, if (use24Hour) "Hm" else "hm"
        )
        val timeFormatter = DateTimeFormatter.ofPattern(timePattern, locale)
        val dateTimeFormatter = DateTimeFormatter.ofPattern(
            android.text.format.DateFormat.getBestDateTimePattern(
                locale, if (use24Hour) "yMdHm" else "yMdhm"
            ), locale
        )
        val now = LocalDateTime.now()
        val dateSaved =
            Instant.ofEpochMilli(createAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
        if (now.toLocalDate().isEqual(dateSaved.toLocalDate())) {
            context.getString(R.string.today_indicator_measure, timeFormatter.format(dateSaved))
        } else {
            dateTimeFormatter.format(dateSaved)
        }
    }

    Text(
        text = dateString,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
    )
}
