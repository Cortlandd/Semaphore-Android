package com.cortlandwalker.semaphore.features.workoutlist

import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.models.Workout

data class WorkoutListState(
    val workouts: List<Workout> = emptyList(),
    var changingText: String = "",
    val error: String? = null,
    val showBannerAd: Boolean = false,
    val isPlayingAll: Boolean = false,
    val isPlaybackPaused: Boolean = false,
    val playbackQueue: List<String> = emptyList(),
    val activeWorkoutId: String? = null,
    val activeWorkoutTimer: String? = null,
    val displayMode: ViewDisplayMode = ViewDisplayMode.Empty
)

sealed interface WorkoutListAction {
    data object OnLoad : WorkoutListAction
    data object PauseTapped : WorkoutListAction
    data object ResumeTapped : WorkoutListAction
    data object StopTapped : WorkoutListAction
    data class UpdatePosition(val workout: Workout, val position: Int) : WorkoutListAction
    data object TappedSettings : WorkoutListAction
    data object TappedAddWorkout : WorkoutListAction
    data class TappedWorkout(val workout: Workout) : WorkoutListAction
    /** Delete a workout by id. */
    data class DeleteTapped(val id: String) : WorkoutListAction

    /** Finalize a drag-and-drop reorder: order is list of workout IDs top->bottom. */
    data class ReorderCommit(val orderedIds: List<String>) : WorkoutListAction
    data class SinglePlayTapped(val id: String) : WorkoutListAction
    data class BannerAdVisibilityChanged(val visible: Boolean) : WorkoutListAction
    data object PlayAllTapped : WorkoutListAction
}

sealed interface WorkoutListEffect {
    data object NavSettings : WorkoutListEffect
    data object NavAddWorkout : WorkoutListEffect
    data class NavEditWorkout(val workoutId: String) : WorkoutListEffect
}
