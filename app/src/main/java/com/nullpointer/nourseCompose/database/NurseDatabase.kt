package com.nullpointer.nourseCompose.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nullpointer.nourseCompose.data.measure.local.MeasureDAO
import com.nullpointer.nourseCompose.data.medication.local.MedicationReminderDao
import com.nullpointer.nourseCompose.models.entity.MeasureEntity
import com.nullpointer.nourseCompose.models.entity.MedicationReminderEntity
import com.nullpointer.nourseCompose.models.entity.AlarmLogEntity


@Database(
    version = 9,
    entities = [MeasureEntity::class, MedicationReminderEntity::class, AlarmLogEntity::class],
    exportSchema = false
)
abstract class NurseDatabase : RoomDatabase() {

    abstract fun getMeasureDAO(): MeasureDAO
    abstract fun getMedicationReminderDao(): MedicationReminderDao
    abstract fun getAlarmLogDao(): com.nullpointer.nourseCompose.data.alarm.local.AlarmLogDao
}
