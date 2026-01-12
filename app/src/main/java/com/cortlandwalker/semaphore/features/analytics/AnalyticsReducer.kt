package com.cortlandwalker.semaphore.features.analytics

import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AnalyticsReducer @Inject constructor(private val repo: WorkoutRepository) :
    Reducer<AnalyticsState, AnalyticsAction, AnalyticsEffect>() {

    override fun onLoadAction(): AnalyticsAction = AnalyticsAction.OnLoad

    override suspend fun process(action: AnalyticsAction) {
        when (action) {
            AnalyticsAction.OnLoad -> {
                collectLocalOnce(
                    key = "workouts",
                    flow = repo.observeAllOrderedByPosition(),
                    onEach = { workouts ->
                        state { calculateSummaryMetrics(workouts) }
                    }
                )
            }
            AnalyticsAction.TapBack -> emit(AnalyticsEffect.NavBack)
        }
    }

    private fun calculateSummaryMetrics(workouts: List<Workout>): AnalyticsState {
        val totalWorkouts = workouts.sumOf { it.completedCount }
        val totalHours = workouts.sumOf { it.totalTimeSpentSeconds }.toFloat() / 3600f
        val currentStreak = workouts.maxOfOrNull { it.currentStreak } ?: 0

        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis

        val distinctDays = workouts
            .filter { it.lastPerformedAt != null && it.lastPerformedAt >= sevenDaysAgo }
            .distinctBy { getDayIdentifier(it.lastPerformedAt!!) }
            .count()

        val weeklyProgress = (distinctDays / 5.0f).coerceAtMost(1.0f)

        return currentState.copy(
            workouts = workouts,
            totalWorkouts = totalWorkouts,
            totalHours = totalHours,
            currentStreak = currentStreak,
            weeklyProgress = weeklyProgress
        )
    }

    private fun getDayIdentifier(timestamp: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }
}
