package com.nullpointer.nourseCompose.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object NurseDatabaseMigrations {
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE alarm_logs ADD COLUMN category TEXT NOT NULL DEFAULT 'ALARM'")
            database.execSQL("ALTER TABLE alarm_logs ADD COLUMN severity TEXT NOT NULL DEFAULT 'INFO'")
            database.execSQL("ALTER TABLE alarm_logs ADD COLUMN stackTrace TEXT")
        }
    }
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS alarm_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    reminderId INTEGER,
                    reminderName TEXT NOT NULL,
                    eventType TEXT NOT NULL,
                    occurredAt INTEGER NOT NULL,
                    success INTEGER NOT NULL,
                    details TEXT,
                    isFirstReminder INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
        }
    }
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS medication_reminders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    dosage TEXT,
                    comment TEXT,
                    photoUri TEXT,
                    startAt INTEGER NOT NULL,
                    endAt INTEGER,
                    intervalHours INTEGER NOT NULL,
                    isActive INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE medication_reminders ADD COLUMN useExactAlarm INTEGER NOT NULL DEFAULT 0")
        }
    }
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE medication_reminders ADD COLUMN notificationMode TEXT NOT NULL DEFAULT 'NOTIFICATION'")
            database.execSQL("ALTER TABLE medication_reminders ADD COLUMN vibrationEnabled INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE medication_reminders ADD COLUMN soundEnabled INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE medication_reminders ADD COLUMN fullScreenAlarm INTEGER NOT NULL DEFAULT 0")
        }
    }
}
