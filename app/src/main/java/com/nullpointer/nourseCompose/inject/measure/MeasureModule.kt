package com.nullpointer.nourseCompose.inject.measure

import com.nullpointer.nourseCompose.data.csv.local.BackUpDatabase
import com.nullpointer.nourseCompose.data.measure.local.MeasureDAO
import com.nullpointer.nourseCompose.database.NurseDatabase
import com.nullpointer.nourseCompose.datasource.measure.local.MeasureLocalDataSource
import com.nullpointer.nourseCompose.datasource.measure.local.MeasureLocalDataSourceImpl
import com.nullpointer.nourseCompose.datasource.settings.local.SettingsLocalDataSource
import com.nullpointer.nourseCompose.domain.measure.MeasureRepoImpl
import com.nullpointer.nourseCompose.domain.measure.MeasureRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MeasureModule {

    @Provides
    @Singleton
    fun provideMeasureDAO(
        nurseDatabase: NurseDatabase
    ): MeasureDAO = nurseDatabase.getMeasureDAO()


    @Provides
    @Singleton
    fun provideMeasureLocalDataSource(
        measureDAO: MeasureDAO,
        backUpDatabase: BackUpDatabase
    ): MeasureLocalDataSource = MeasureLocalDataSourceImpl(
        measureDAO = measureDAO,
        backUpDatabase = backUpDatabase
    )

    @Provides
    @Singleton
    fun provideMeasureRepository(
        measureLocalDataSource: MeasureLocalDataSource,
        settingsLocalDataSource: SettingsLocalDataSource
    ): MeasureRepository = MeasureRepoImpl(
        measureLocalDataSource = measureLocalDataSource,
        settingsLocalDataSource = settingsLocalDataSource
    )

}