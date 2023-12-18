package com.nullpointer.nourseCompose.ui.models.data

import com.nullpointer.nourseCompose.ui.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.ui.models.types.MeasureType

data class MeasureData(
    val id: Int,
    val value: Double,
    val createAt: Long,
    val type: MeasureType,
) {

    companion object {
        fun fromMeasureEntity(measureEntity: MeasureEntity): MeasureData {
            return MeasureData(
                id = measureEntity.id,
                type = measureEntity.type,
                value = measureEntity.value,
                createAt = measureEntity.createAt,
            )
        }
    }
}
