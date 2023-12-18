package com.nullpointer.nourseCompose.inject.database

import android.content.Context
import androidx.room.Room
import com.nullpointer.nourseCompose.database.NurseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MeasureDatabaseModule {

    private const val nameDatabase = "measure.db"
    private const val keyNameDatabase = "keyNameDatabase"

    @Provides
    @Singleton
    @Named(value = keyNameDatabase)
    fun provideNameDatabase(): String = nameDatabase


    @Provides
    @Singleton
    fun provideNurseDatabase(
        @ApplicationContext context: Context,
        @Named(value = keyNameDatabase) nameDatabase: String
    ): NurseDatabase = Room.databaseBuilder(
        context = context,
        name = nameDatabase,
        klass = NurseDatabase::class.java
    ).fallbackToDestructiveMigration().build()


}