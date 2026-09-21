package com.nullpointer.nourseCompose.inject.medication

import com.nullpointer.nourseCompose.data.medication.local.MedicationReminderDao
import com.nullpointer.nourseCompose.database.NurseDatabase
import com.nullpointer.nourseCompose.datasource.medication.local.MedicationReminderLocalDataSource
import com.nullpointer.nourseCompose.datasource.medication.local.MedicationReminderLocalDataSourceImpl
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepoImpl
import com.nullpointer.nourseCompose.domain.medication.MedicationReminderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MedicationReminderModule {
    @Provides
    @Singleton
    fun provideMedicationReminderDao(database: NurseDatabase): MedicationReminderDao =
        database.getMedicationReminderDao()

    @Provides
    @Singleton
    fun provideMedicationReminderLocalDataSource(
        dao: MedicationReminderDao,
    ): MedicationReminderLocalDataSource = MedicationReminderLocalDataSourceImpl(dao)

    @Provides
    @Singleton
    fun provideMedicationReminderRepository(
        localDataSource: MedicationReminderLocalDataSource,
    ): MedicationReminderRepository = MedicationReminderRepoImpl(localDataSource)
}