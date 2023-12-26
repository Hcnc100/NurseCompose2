package com.nullpointer.nourseCompose.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nullpointer.nourseCompose.contants.Constants
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType

@Entity(tableName = Constants.MEASURE_TABLE_NAME)
data class MeasureEntity(
    @ColumnInfo(name = Constants.MEASURE_ID_NAME)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = Constants.MEASURE_VALUE1_NAME)
    val value1: Float,
    @ColumnInfo(name = Constants.MEASURE_VALUE2_NAME)
    val value2: Float? = null,
    @ColumnInfo(name = Constants.MEASURE_TYPE_NAME)
    val type: MeasureType,
    @ColumnInfo(name = Constants.MEASURE_CREATE_AT_NAME)
    val createAt: Long = System.currentTimeMillis(),
) {
    companion object {
        fun fromMeasureData(measureData: MeasureData): MeasureEntity {
            return MeasureEntity(
                id = measureData.id,
                value1 = measureData.value1,
                value2 = measureData.value2,
                type = measureData.type,
            )
        }
    }
}