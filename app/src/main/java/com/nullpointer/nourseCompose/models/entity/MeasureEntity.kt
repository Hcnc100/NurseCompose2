package com.nullpointer.nourseCompose.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType

@Entity(tableName = "measures")
data class MeasureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val value1: Float,
    val value2: Float? = null,
    val type: MeasureType,
    val createAt: Long = System.currentTimeMillis(),
) {
    companion object {
        fun fromMeasureData(measureData: MeasureData): MeasureEntity {
            return MeasureEntity(
                value1 = measureData.value1,
                value2 = measureData.value2,
                type = measureData.type,
            )
        }
    }
}