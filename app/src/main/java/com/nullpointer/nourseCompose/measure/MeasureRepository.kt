package com.nullpointer.nourseCompose.measure

import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow

interface MeasureRepository {
    fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>>

    suspend fun addMeasure(measureData: MeasureData)

    suspend fun deleterMeasureData(measureData: MeasureData)

    suspend fun updateMeasureData(measureData: MeasureData)
}