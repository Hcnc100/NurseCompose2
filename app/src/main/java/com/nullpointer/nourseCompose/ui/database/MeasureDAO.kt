package com.nullpointer.nourseCompose.ui.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nullpointer.nourseCompose.ui.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.ui.models.types.MeasureType
import kotlinx.coroutines.flow.Flow


@Dao
interface MeasureDAO {

    @Query("SELECT * FROM measures WHERE type = (:type) ORDER BY dateInMillis DESC LIMIT (:limit)")
    fun getListMeasureByTypes(type: MeasureType, limit: Int): Flow<List<MeasureEntity>>

    @Insert
    suspend fun insertMeasure(measureEntity: MeasureEntity)

    @Update
    suspend fun updateMeasure(measureEntity: MeasureEntity)

    @Update
    suspend fun deleter(measureEntity: MeasureEntity)
}