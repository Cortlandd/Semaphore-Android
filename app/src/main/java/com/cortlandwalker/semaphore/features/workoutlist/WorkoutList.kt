package com.cortlandwalker.semaphore.features.workoutlist

import com.cortlandwalker.semaphore.data.models.Workout

data class WorkoutListState(
    val workouts: List<Workout> = emptyList(),
    var changingText: String = "testing",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface WorkoutListAction {
    data object OnLoad : WorkoutListAction
    data class UpdatePosition(val workout: Workout, val position: Int) : WorkoutListAction
    data object TappedSettings : WorkoutListAction
    data object TappedAddWorkout : WorkoutListAction
    data class TappedWorkout(val workout: Workout) : WorkoutListAction
    /** Delete a workout by id. */
    data class DeleteTapped(val id: String) : WorkoutListAction

    /** Finalize a drag-and-drop reorder: order is list of workout IDs top->bottom. */
    data class ReorderCommit(val orderedIds: List<String>) : WorkoutListAction
    data class SinglePlayTapped(val id: String) : WorkoutListAction
    data object PlayAllTapped : WorkoutListAction
}

sealed interface WorkoutListEffect {
    data object NavSettings : WorkoutListEffect
    data object NavAddWorkout : WorkoutListEffect
    data class NavEditWorkout(val workoutId: String) : WorkoutListEffect
}