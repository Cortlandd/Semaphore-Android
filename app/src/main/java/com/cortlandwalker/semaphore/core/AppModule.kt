package com.cortlandwalker.semaphore.core

import android.content.Context
import androidx.room.Room
import com.cortlandwalker.semaphore.data.local.room.RoomWorkoutRepository
import com.cortlandwalker.semaphore.data.local.room.SemaphoreDatabase
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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SemaphoreDatabase =
        Room.databaseBuilder(ctx, SemaphoreDatabase::class.java, "semaphore.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideWorkoutDao(db: SemaphoreDatabase): WorkoutDao = db.workoutDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoBindsModule {
    @Binds @Singleton
    abstract fun bindWorkoutRepository(impl: RoomWorkoutRepository): WorkoutRepository
}