package com.nullpointer.nourseCompose.datasource.measure.local

import com.nullpointer.nourseCompose.database.MeasureDAO
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasureLocalDataSourceImpl(
    private val measureDAO: MeasureDAO
) : MeasureLocalDataSource {
    override fun getListMeasureByType(type: MeasureType): Flow<List<MeasureData>> =
        measureDAO.getListMeasureByTypes(type).map { measureList ->
            measureList.map(MeasureData::fromMeasureEntity)
        }

    override suspend fun addMeasure(value: Float, type: MeasureType) {
        val measureEntity = MeasureEntity(
            value = value,
            type = type
        )
        measureDAO.insertMeasure(measureEntity)
    }


    override suspend fun deleterMeasureData(measureData: MeasureData) {
        val measureEntity = MeasureEntity.fromMeasureData(measureData)
        measureDAO.deleter(measureEntity)
    }

    override suspend fun updateMeasureData(measureData: MeasureData) {
        val measureEntity = MeasureEntity.fromMeasureData(measureData)
        measureDAO.updateMeasure(measureEntity)
    }
}