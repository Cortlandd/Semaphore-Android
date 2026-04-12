package com.cortlandwalker.semaphore.features.analytics

import com.cortlandwalker.semaphore.data.models.Workout
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnalyticsReducerTest {

    @Test
    fun `buildAnalyticsState should calculate weekly progress and top workouts`() {
        val now = System.currentTimeMillis()
        val workouts = listOf(
            Workout(
                id = "1",
                createdAt = now,
                name = "Push Ups",
                imageUri = null,
                hours = 0,
                minutes = 0,
                seconds = 30,
                position = 0,
                orderId = 0,
                completedCount = 8,
                totalTimeSpentSeconds = 2400,
                lastPerformedAt = now,
                currentStreak = 4
            ),
            Workout(
                id = "2",
                createdAt = now,
                name = "Squats",
                imageUri = null,
                hours = 0,
                minutes = 1,
                seconds = 0,
                position = 1,
                orderId = 0,
                completedCount = 4,
                totalTimeSpentSeconds = 3600,
                lastPerformedAt = now - 86_400_000L,
                currentStreak = 2
            ),
            Workout(
                id = "3",
                createdAt = now,
                name = "Plank",
                imageUri = null,
                hours = 0,
                minutes = 0,
                seconds = 45,
                position = 2,
                orderId = 0,
                completedCount = 1,
                totalTimeSpentSeconds = 45,
                lastPerformedAt = now - (8 * 86_400_000L),
                currentStreak = 1
            )
        )

        val state = buildAnalyticsState(AnalyticsState(), workouts)
        assertThat(state.totalWorkouts).isEqualTo(13)
        assertThat(state.currentStreak).isEqualTo(4)
        assertThat(state.weeklyActiveDays).isEqualTo(2)
        assertThat(state.weeklyProgress).isEqualTo(0.4f)
        assertThat(state.topWorkouts.map { it.id }).containsExactly("2", "1", "3").inOrder()
    }
}
