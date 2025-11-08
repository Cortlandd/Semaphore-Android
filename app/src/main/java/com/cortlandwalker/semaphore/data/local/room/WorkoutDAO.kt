package com.cortlandwalker.semaphore.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cortlandwalker.semaphore.data.models.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY position ASC, createdAt ASC")
    fun observeAllOrderedByPosition(): Flow<List<Workout>>
    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Workout?
    @Query("SELECT COALESCE(MAX(position), -1) FROM workouts")
    suspend fun maxPosition(): Int
    @Query("UPDATE workouts SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: String, position: Int)
    @Insert
    suspend fun insert(workout: Workout)
    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun delete(id: String)
    @Update
    suspend fun update(workout: Workout)
}
