package com.nullpointer.nourseCompose.local

import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow

interface MeasureLocalDataSource {

    fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>>

    suspend fun addMeasure(measureData: MeasureData)

    suspend fun deleterMeasureData(measureData: MeasureData)

    suspend fun updateMeasureData(measureData: MeasureData)
}