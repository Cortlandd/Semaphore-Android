package com.cortlandwalker.semaphore.features.workoutlist

import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import com.cortlandwalker.semaphore.features.workoutlist.WorkoutListEffect.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class WorkoutListReducer(
    private val repo: WorkoutRepository,
    private val tickDelayMillis: Long
) :
    Reducer<WorkoutListState, WorkoutListAction, WorkoutListEffect>() {

    @Inject
    constructor(repo: WorkoutRepository) : this(repo, 1_000L)

    private var timerJob: Job? = null

    override fun onLoadAction(): WorkoutListAction? = WorkoutListAction.OnLoad

    override suspend fun process(action: WorkoutListAction) {
        when (action) {
            WorkoutListAction.OnLoad -> {
                scope.launch {
                    repo.observeAllOrderedByPosition()
                        .catch { throwable ->
                            // Handle any errors from the database flow
                            state {
                                it.copy(
                                    displayMode = ViewDisplayMode.Error,
                                    error = throwable.message ?: "Failed to load workouts"
                                )
                            }
                        }
                        .collect { itemsFromDb ->
                            state { currentState ->
                                val currentIds = currentState.workouts.map { it.id }
                                val newIds = itemsFromDb.map { it.id }

                                val currentSet = currentIds.toSet()
                                val newSet = newIds.toSet()

                                if (currentSet == newSet && currentIds != newIds) {
                                    return@state currentState
                                }

                                if (currentState.workouts == itemsFromDb) {
                                    return@state currentState
                                }

                                currentState.copy(
                                    workouts = itemsFromDb,
                                    displayMode = ViewDisplayMode.Content,
                                    error = null
                                )
                            }
                        }
                }
            }
            WorkoutListAction.TappedSettings -> { emit(WorkoutListEffect.NavSettings) }
            WorkoutListAction.TappedAddWorkout -> {
                emit(WorkoutListEffect.NavAddWorkout)
            }
            is WorkoutListAction.DeleteTapped -> { repo.deleteById(action.id) }
            is WorkoutListAction.ReorderCommit -> {
                val finalOrderedIds = currentState.workouts.map { it.id }

                scope.launch {
                    repo.updatePositions(finalOrderedIds)
                }
            }
            is WorkoutListAction.UpdatePosition -> {
                state { currentState ->
                    val fromIndex = currentState.workouts.indexOfFirst { it.id == action.workout.id }
                    val toIndex = action.position

                    if (fromIndex == -1 || toIndex !in currentState.workouts.indices) {
                        return@state currentState
                    }

                    val newList = currentState.workouts.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                    currentState.copy(workouts = newList)
                }
            }
            is WorkoutListAction.TappedWorkout -> {
                emit(NavEditWorkout(action.workout.id))
            }
            is WorkoutListAction.BannerAdVisibilityChanged -> {
                state { it.copy(showBannerAd = action.visible) }
            }
            WorkoutListAction.PlayAllTapped -> {
                if (currentState.isPlayingAll) {
                    stopWorkout()
                    return
                }

                startPlayback(
                    workoutIds = currentState.workouts.map { it.id },
                    playAll = true
                )
            }
            is WorkoutListAction.SinglePlayTapped -> {
                if (currentState.activeWorkoutId == action.id) {
                    stopWorkout()
                    return
                }

                startPlayback(
                    workoutIds = listOf(action.id),
                    playAll = false
                )
            }
            WorkoutListAction.StopTapped -> {
                stopWorkout()
            }
        }
    }

    private fun startPlayback(workoutIds: List<String>, playAll: Boolean) {
        if (workoutIds.isEmpty()) {
            stopWorkout()
            return
        }

        timerJob?.cancel()
        timerJob = scope.launch {
            for ((index, workoutId) in workoutIds.withIndex()) {
                val workout = currentState.workouts.find { it.id == workoutId } ?: continue
                val durationSeconds = workout.durationSeconds()
                var remainingSeconds = durationSeconds

                state {
                    it.copy(
                        isPlayingAll = playAll,
                        playbackQueue = if (playAll) workoutIds.drop(index + 1) else emptyList(),
                        activeWorkoutId = workoutId,
                        activeWorkoutTimer = formatSecondsToHms(remainingSeconds)
                    )
                }

                while (remainingSeconds >= 0) {
                    state { it.copy(activeWorkoutTimer = formatSecondsToHms(remainingSeconds)) }
                    if (remainingSeconds == 0) {
                        updateWorkoutAnalytics(workout, durationSeconds)
                        break
                    }
                    delay(tickDelayMillis)
                    remainingSeconds--
                }
            }

            clearPlaybackState(cancelTimer = false)
        }
    }

    private suspend fun updateWorkoutAnalytics(workout: Workout, durationSeconds: Int) {
        // Calculate new stats
        val newStreak = if (wasPerformedYesterday(workout.lastPerformedAt)) workout.currentStreak + 1 else 1
        val updatedWorkout = workout.copy(
            completedCount = workout.completedCount + 1,
            totalTimeSpentSeconds = workout.totalTimeSpentSeconds + durationSeconds,
            lastPerformedAt = System.currentTimeMillis(),
            currentStreak = newStreak
        )
        // Persist to DB
        repo.update(updatedWorkout)
    }

    private fun wasPerformedYesterday(lastPerformedAt: Long?): Boolean {
        if (lastPerformedAt == null) return false

        val lastDate = Calendar.getInstance().apply { timeInMillis = lastPerformedAt }
        val today = Calendar.getInstance()

        // Reset time part for accurate date comparison
        lastDate.set(Calendar.HOUR_OF_DAY, 0)
        lastDate.set(Calendar.MINUTE, 0)
        lastDate.set(Calendar.SECOND, 0)
        lastDate.set(Calendar.MILLISECOND, 0)

        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        today.add(Calendar.DAY_OF_YEAR, -1)

        return lastDate.timeInMillis == today.timeInMillis
    }


    private fun stopWorkout() {
        clearPlaybackState(cancelTimer = true)
    }

    private fun clearPlaybackState(cancelTimer: Boolean) {
        if (cancelTimer) {
            timerJob?.cancel()
        }

        timerJob = null
        state {
            it.copy(
                isPlayingAll = false,
                playbackQueue = emptyList(),
                activeWorkoutId = null,
                activeWorkoutTimer = null
            )
        }
    }

    private fun formatSecondsToHms(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) {
            "%02d:%02d:%02d".format(h, m, s)
        } else {
            "%02d:%02d".format(m, s)
        }
    }

    private fun Workout.durationSeconds(): Int {
        return (hours * 3600) + (minutes * 60) + seconds
    }
}
