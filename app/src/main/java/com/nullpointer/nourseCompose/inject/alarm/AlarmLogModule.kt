package com.nullpointer.nourseCompose.inject.alarm

import com.nullpointer.nourseCompose.data.alarm.local.AlarmLogDao
import com.nullpointer.nourseCompose.database.NurseDatabase
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepoImpl
import com.nullpointer.nourseCompose.domain.alarm.AlarmLogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmLogModule {
    @Provides
    @Singleton
    fun provideAlarmLogDao(database: NurseDatabase): AlarmLogDao = database.getAlarmLogDao()

    @Provides
    @Singleton
    fun provideAlarmLogRepository(dao: AlarmLogDao): AlarmLogRepository = AlarmLogRepoImpl(dao)
}
