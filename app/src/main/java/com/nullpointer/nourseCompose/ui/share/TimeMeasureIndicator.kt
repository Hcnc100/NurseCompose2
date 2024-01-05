package com.nullpointer.nourseCompose.ui.share

import android.content.Context
import androidx.compose.material.Text
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

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
fun TimeMeasureIndicator(
    createAt: Long,
    context: Context = LocalContext.current
) {


    val dateString = remember(createAt) {
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
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
        ),
    )
}
