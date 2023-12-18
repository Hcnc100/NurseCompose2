package com.nullpointer.nourseCompose.inject.measure

import com.nullpointer.nourseCompose.database.MeasureDAO
import com.nullpointer.nourseCompose.database.NurseDatabase
import com.nullpointer.nourseCompose.datasource.measure.local.MeasureLocalDataSource
import com.nullpointer.nourseCompose.datasource.measure.local.MeasureLocalDataSourceImpl
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
        measureDAO: MeasureDAO
    ): MeasureLocalDataSource = MeasureLocalDataSourceImpl(
        measureDAO = measureDAO
    )

    @Provides
    @Singleton
    fun provideMeasureRepository(
        measureLocalDataSource: MeasureLocalDataSource
    ): MeasureRepository = MeasureRepoImpl(
        measureLocalDataSource = measureLocalDataSource
    )

}