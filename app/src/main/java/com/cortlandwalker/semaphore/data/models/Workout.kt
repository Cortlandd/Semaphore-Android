package com.cortlandwalker.semaphore.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey val id: String, // UUID
    val createdAt: Long,
    val name: String,
    val imageUri: String?,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    var position: Int,
    val orderId: Int,

    // --- Analytics Fields ---

    /** How many times this workout has fully completed (timer hit 0). */
    val completedCount: Int = 0,

    /**
     * Cumulative seconds spent active on this workout.
     * Useful for: "You've spent 3 hours total on Plank".
     */
    val totalTimeSpentSeconds: Long = 0,

    /**
     * Timestamp (ms) of the last time this specific workout was finished.
     * Useful for: Sorting by "Recently performed" or "Neglected".
     */
    val lastPerformedAt: Long? = null,

    /**
     * Streak count? (Optional, maybe overkill for now, but good for future)
     * e.g. Number of consecutive days performed.
     */
    // val currentStreak: Int = 0
)