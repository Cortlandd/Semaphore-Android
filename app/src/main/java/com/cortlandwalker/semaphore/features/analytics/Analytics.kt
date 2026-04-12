package com.cortlandwalker.semaphore.features.analytics

import com.cortlandwalker.semaphore.data.models.Workout

data class AnalyticsState(
    val workouts: List<Workout> = emptyList(),
    val totalWorkouts: Int = 0,
    val totalHours: Float = 0f,
    val currentStreak: Int = 0,
    val weeklyProgress: Float = 0f,
    val weeklyActiveDays: Int = 0,
    val weeklyGoalDays: Int = 5,
    val topWorkouts: List<WorkoutAnalyticsEntry> = emptyList()
)

data class WorkoutAnalyticsEntry(
    val id: String,
    val name: String,
    val completedCount: Int,
    val totalTimeSpentSeconds: Long
)

sealed interface AnalyticsAction {
    data object OnLoad : AnalyticsAction
    data object TapBack : AnalyticsAction
}

sealed interface AnalyticsEffect {
    data object NavBack : AnalyticsEffect
}
