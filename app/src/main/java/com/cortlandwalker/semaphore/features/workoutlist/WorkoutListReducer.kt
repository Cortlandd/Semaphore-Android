package com.cortlandwalker.semaphore.features.workoutlist

import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import com.cortlandwalker.semaphore.features.workoutlist.WorkoutListEffect.*
import com.cortlandwalker.semaphore.playback.WorkoutPlaybackController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class WorkoutListReducer @Inject constructor(
    private val repo: WorkoutRepository,
    private val playbackController: WorkoutPlaybackController
) :
    Reducer<WorkoutListState, WorkoutListAction, WorkoutListEffect>() {

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

                scope.launch {
                    playbackController.playbackState.collect { playbackState ->
                        state {
                            it.copy(
                                isPlayingAll = playbackState.isPlayingAll,
                                isPlaybackPaused = playbackState.isPaused,
                                playbackQueue = playbackState.playbackQueue,
                                activeWorkoutId = playbackState.activeWorkoutId,
                                activeWorkoutTimer = playbackState.activeWorkoutTimer
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
                scope.launch {
                    repo.updatePositions(action.orderedIds)
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
                    playbackController.stop()
                    return
                }

                playbackController.startAll(currentState.workouts)
            }
            WorkoutListAction.PauseTapped -> {
                playbackController.pause()
            }
            WorkoutListAction.ResumeTapped -> {
                playbackController.resume()
            }
            is WorkoutListAction.SinglePlayTapped -> {
                if (currentState.activeWorkoutId == action.id) {
                    playbackController.stop()
                    return
                }

                val workout = currentState.workouts.find { it.id == action.id } ?: return
                playbackController.startSingle(workout)
            }
            WorkoutListAction.StopTapped -> {
                playbackController.stop()
            }
        }
    }
}
