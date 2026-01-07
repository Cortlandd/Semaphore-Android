package com.cortlandwalker.semaphore.features.workoutlist

import android.util.Log
import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.features.workoutlist.WorkoutListEffect.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class WorkoutListReducer @Inject constructor(private val repo: WorkoutRepository) :
    Reducer<WorkoutListState, WorkoutListAction, WorkoutListEffect>() {

    private var timerJob: Job? = null

    override fun onLoadAction(): WorkoutListAction? = WorkoutListAction.OnLoad

    override suspend fun process(action: WorkoutListAction) {
        when (action) {
            WorkoutListAction.OnLoad -> {
                collectLocalOnce(
                    key = "workouts",
                    flow = repo.observeAllOrderedByPosition(),
                    onEach = { items ->
                        state { currentState ->
                            val currentIds = currentState.workouts.map { it.id }
                            val newIds = items.map { it.id }

                            val currentSet = currentIds.toSet()
                            val newSet = newIds.toSet()

                            if (currentSet == newSet && currentIds != newIds) {
                                currentState
                            } else if (currentState.workouts == items) {
                                currentState
                            } else {
                                currentState.copy(workouts = items, displayMode = ViewDisplayMode.Content, error = null)
                            }
                        }
                    },
                    onError = { t ->
                        state { it.copy(displayMode = ViewDisplayMode.Error, error = t.message ?: "Failed to load workouts") }
                    }
                )
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

                    // This is a more robust way to move items that Compose understands better
                    val newList = currentState.workouts.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                    currentState.copy(workouts = newList)
                }
            }
            is WorkoutListAction.TappedWorkout -> {
                emit(NavEditWorkout(action.workout.id))
            }
            WorkoutListAction.PlayAllTapped -> {
                val firstWorkoutId = currentState.workouts.firstOrNull()?.id
                state { it.copy(activeWorkoutId = firstWorkoutId) }
            }
            is WorkoutListAction.SinglePlayTapped -> {
                if (currentState.activeWorkoutId == action.id) {
                    stopWorkout()
                    return
                }

                // Cancel existing timer
                timerJob?.cancel()

                val workout = currentState.workouts.find { it.id == action.id } ?: return

                // Expand row
                state { it.copy(activeWorkoutId = action.id) }

                // Start the countdown coroutine
                timerJob = scope.launch {
                    var remainingSeconds = (workout.hours * 3600) + (workout.minutes * 60) + workout.seconds

                    while (remainingSeconds >= 0) {
                        // Update the display string in state
                        state { it.copy(activeWorkoutTimer = formatSecondsToHms(remainingSeconds)) }
                        if (remainingSeconds == 0) break
                        delay(1000L)
                        remainingSeconds--
                    }

                    Log.d("WorkoutListReducer", "Timer finished")

                    // 4. Auto-collapse when finished
                    stopWorkout()

//                    val currentIndex = currentState.workouts.indexOfFirst { it.id == action.id }
//                    val nextWorkout = currentState.workouts.getOrNull(currentIndex + 1)
//                    if (nextWorkout != null) {
//                        process(SinglePlayTapped(nextWorkout.id))
//                    } else {
//                        stopWorkout()
//                    }
                }
            }
            WorkoutListAction.StopTapped -> {
                stopWorkout()
            }
        }
    }

    private fun stopWorkout() {
        timerJob?.cancel()
        timerJob = null
        state { it.copy(activeWorkoutId = null, activeWorkoutTimer = null) }
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

}