package com.nullpointer.nourseCompose.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nullpointer.nourseCompose.models.entity.MeasureEntity


@Database(
    version = 2,
    entities = [MeasureEntity::class],
    exportSchema = true
)
abstract class NurseDatabase : RoomDatabase() {

    abstract fun getMeasureDAO(): MeasureDAO
}