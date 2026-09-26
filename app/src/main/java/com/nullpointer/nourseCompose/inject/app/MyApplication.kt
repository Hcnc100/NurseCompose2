package com.nullpointer.nourseCompose.inject.app

import android.app.Application
import com.nullpointer.nourseCompose.BuildConfig
import com.nullpointer.nourseCompose.notifications.MedicationReminderScheduler
import com.nullpointer.nourseCompose.domain.alarm.AppLogger
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application() {

    @javax.inject.Inject lateinit var appLogger: AppLogger

    override fun onCreate() {
        super.onCreate()
        MedicationReminderScheduler.createChannel(this)
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            appLogger.recordCrash(thread.name, throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }

        if (!BuildConfig.DEBUG) {
            return
        }

        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true)
            .methodCount(1)
            .methodOffset(5)
            .tag("")
            .build()

        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))


        Timber.plant(object : Timber.DebugTree() {

            override fun log(
                priority: Int, tag: String?, message: String, t: Throwable?,
            ) {
                Logger.log(priority, "@@", message, t)
            }
        })
    }


}
