package com.nullpointer.nourseCompose.datasource.measure.local

import androidx.paging.PagingSource
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

interface MeasureLocalDataSource {

    fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>>

    fun getPagingMeasureByType(type: MeasureType): PagingSource<Int, MeasureEntity>

    suspend fun addMeasure(type: MeasureType, value1: Float, value2: Float?)

    suspend fun deleterMeasureData(measureData: MeasureData)

    suspend fun updateMeasureData(measureData: MeasureData)

    suspend fun exportDatabase(outputStream: OutputStream)

    suspend fun importDatabase(inputStream: InputStream)
    suspend fun deleterAllMeasures()
}