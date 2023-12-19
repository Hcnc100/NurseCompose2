package com.nullpointer.nourseCompose.models.data

import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class MeasureData(
    val id: Int,
    val value: Float,
    val createAt: String,
    val type: MeasureType,
) {

    companion object {

        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")


        fun fromMeasureEntity(measureEntity: MeasureEntity): MeasureData {
            val now = LocalDateTime.now()
            val dateSaved =
                Instant.ofEpochMilli(measureEntity.createAt).atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

            val dateString = if (now.toLocalDate().isEqual(dateSaved.toLocalDate())) {
                "Today ${timeFormatter.format(dateSaved)}"
            } else {
                dateTimeFormatter.format(dateSaved)
            }


            return MeasureData(
                id = measureEntity.id,
                type = measureEntity.type,
                value = measureEntity.value,
                createAt = dateString,
            )
        }
    }
}
