package com.nullpointer.nourseCompose.datasource.measure.local

import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow

interface MeasureLocalDataSource {

    fun getListMeasureByType(type: MeasureType): Flow<List<MeasureData>>

    suspend fun addMeasure(value: Float, type: MeasureType)

    suspend fun deleterMeasureData(measureData: MeasureData)

    suspend fun updateMeasureData(measureData: MeasureData)
}