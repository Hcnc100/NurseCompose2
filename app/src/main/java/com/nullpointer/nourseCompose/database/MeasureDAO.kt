package com.nullpointer.nourseCompose.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.types.MeasureType
import kotlinx.coroutines.flow.Flow


@Dao
interface MeasureDAO {

    @Query("SELECT * FROM measures WHERE type = (:type) ORDER BY createAt DESC")
    fun getListMeasureByTypes(type: MeasureType): Flow<List<MeasureEntity>>

    @Insert
    suspend fun insertMeasure(measureEntity: MeasureEntity)

    @Update
    suspend fun updateMeasure(measureEntity: MeasureEntity)

    @Update
    suspend fun deleter(measureEntity: MeasureEntity)
}