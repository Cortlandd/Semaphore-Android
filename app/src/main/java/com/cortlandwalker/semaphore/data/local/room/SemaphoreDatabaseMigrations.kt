package com.cortlandwalker.semaphore.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SemaphoreDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE workouts ADD COLUMN remoteImageUri TEXT"
            )
        }
    }
}
