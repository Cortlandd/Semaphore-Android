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
     * Number of consecutive days this workout has been performed.
     */
    val currentStreak: Int = 0
)