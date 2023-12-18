package com.nullpointer.nourseCompose.ui.datasource.measure.local

import com.nullpointer.nourseCompose.ui.database.MeasureDAO
import com.nullpointer.nourseCompose.ui.models.data.MeasureData
import com.nullpointer.nourseCompose.ui.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.ui.models.types.MeasureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasureLocalDatSourceImpl(
    private val measureDAO: MeasureDAO
) : MeasureLocalDataSource {
    override fun getListMeasureByType(type: MeasureType, limit: Int): Flow<List<MeasureData>> =
        measureDAO.getListMeasureByTypes(type, limit).map { measureList ->
            measureList.map {
                MeasureData.fromMeasureEntity(it)
            }
        }

    override suspend fun addMeasure(measureData: MeasureData) {
        val measureEntity = MeasureEntity.fromMeasureData(measureData)
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