package com.nullpointer.nourseCompose.domain.measure

import androidx.paging.PagingSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

interface MeasureRepository {
    fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>>

    suspend fun addMeasure(type: MeasureType, value1: Float, value2: Float?)

    suspend fun deleterMeasureData(measureData: MeasureData)

    suspend fun updateMeasureData(measureData: MeasureData)

    fun getPagingMeasureByType(type: MeasureType): PagingSource<Int, MeasureEntity>

    suspend fun exportDatabase(outputStream: OutputStream)

    suspend fun importDatabase(inputStream: InputStream)
    suspend fun deleterAllMeasures()
}