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
    val orderId: Int
)