package com.cortlandwalker.semaphore.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cortlandwalker.semaphore.data.local.room.helpers.Converters
import com.cortlandwalker.semaphore.data.models.Workout

@Database(entities = [Workout::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class SemaphoreDatabase  : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}