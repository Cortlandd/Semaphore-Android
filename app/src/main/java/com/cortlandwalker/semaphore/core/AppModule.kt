package com.cortlandwalker.semaphore.core

import android.content.Context
import androidx.room.Room
import com.cortlandwalker.semaphore.playback.ForegroundWorkoutPlaybackController
import com.cortlandwalker.semaphore.playback.AndroidWorkoutNameSpeaker
import com.cortlandwalker.semaphore.playback.WorkoutNameSpeaker
import com.cortlandwalker.semaphore.playback.WorkoutPlaybackController
import com.cortlandwalker.semaphore.data.local.room.RoomWorkoutRepository
import com.cortlandwalker.semaphore.data.local.room.SemaphoreDatabase
import com.cortlandwalker.semaphore.data.local.room.SemaphoreDatabaseMigrations
import com.cortlandwalker.semaphore.data.local.room.WorkoutDao
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Provides app-scoped Hilt dependencies that are created directly from the
 * Android framework, such as the Room database and DAO layer.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SemaphoreDatabase =
        Room.databaseBuilder(ctx, SemaphoreDatabase::class.java, "semaphore.db")
            .addMigrations(SemaphoreDatabaseMigrations.MIGRATION_1_2)
            .addMigrations(SemaphoreDatabaseMigrations.MIGRATION_2_3)
            .build()

    @Provides @Singleton
    fun provideWorkoutDao(db: SemaphoreDatabase): WorkoutDao = db.workoutDao()
}

/**
 * Binds long-lived interface implementations used across the app so features
 * can depend on abstractions instead of concrete storage or playback classes.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepoBindsModule {
    @Binds @Singleton
    abstract fun bindWorkoutRepository(impl: RoomWorkoutRepository): WorkoutRepository

    @Binds @Singleton
    abstract fun bindWorkoutPlaybackController(
        impl: ForegroundWorkoutPlaybackController
    ): WorkoutPlaybackController

    @Binds @Singleton
    abstract fun bindWorkoutNameSpeaker(
        impl: AndroidWorkoutNameSpeaker
    ): WorkoutNameSpeaker
}
