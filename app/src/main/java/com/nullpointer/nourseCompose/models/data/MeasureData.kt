package com.nullpointer.nourseCompose.models.data

import androidx.compose.runtime.Immutable
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType

@Immutable
data class MeasureData(
    val id: Int,
    val value1: Float,
    val value2: Float?,
    val createAt: Long,
    val type: MeasureType,
) {


    val showValue
        get() = when (type) {
            MeasureType.PRESSURE -> "$value1/$value2 ${type.suffix}"
            else -> "$value1 ${type.suffix}"
        }

    companion object {

        val listMeasureExample = listOf(
            MeasureData(
                id = 0,
                value1 = 118F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
            MeasureData(
                id = 1,
                value1 = 120F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
            MeasureData(
                id = 2,
                value1 = 120F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
            MeasureData(
                id = 3,
                value1 = 120F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
            MeasureData(
                id = 4,
                value1 = 120F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
            MeasureData(
                id = 5,
                value1 = 120F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
            MeasureData(
                id = 6,
                value1 = 120F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
            MeasureData(
                id = 7,
                value1 = 120F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
            MeasureData(
                id = 8,
                value1 = 120F,
                value2 = 80F,
                createAt = 100023,
                type = MeasureType.PRESSURE
            ),
        )

        fun fromMeasureEntity(measureEntity: MeasureEntity): MeasureData {

            return MeasureData(
                id = measureEntity.id,
                type = measureEntity.type,
                value1 = measureEntity.value1,
                value2 = measureEntity.value2,
                createAt = measureEntity.createAt,
            )
        }
    }
}
