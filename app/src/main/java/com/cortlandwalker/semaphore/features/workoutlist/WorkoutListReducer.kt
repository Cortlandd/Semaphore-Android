package com.cortlandwalker.semaphore.features.workoutlist

import android.util.Log
import com.cortlandwalker.semaphore.core.helpers.Reducer
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class WorkoutListReducer @Inject constructor(private val repo: WorkoutRepository) :
    Reducer<WorkoutListState, WorkoutListAction, WorkoutListEffect>() {

    override fun onLoadAction(): WorkoutListAction? = WorkoutListAction.OnLoad

    override suspend fun process(action: WorkoutListAction) {
        when (action) {
            WorkoutListAction.OnLoad -> {
                state { it.copy(isLoading = true, error = null) }
                collectLocalOnce(
                    key = "workouts",
                    flow = repo.observeAllOrderedByPosition(),
                    onEach = { items ->
                        state { it.copy(workouts = items, isLoading = false, error = null) }
                    },
                    onError = { t ->
                        state { it.copy(isLoading = false, error = t.message ?: "Failed to load workouts") }
                    }
                )
            }

            WorkoutListAction.TappedSettings -> { emit(WorkoutListEffect.NavSettings) }
            WorkoutListAction.TappedAddWorkout -> {
                emit(WorkoutListEffect.NavAddWorkout)
            }
            is WorkoutListAction.DeleteTapped -> { repo.deleteById(action.id) }
            is WorkoutListAction.ReorderCommit -> {
                repo.updatePositions(action.orderedIds)
            }
            is WorkoutListAction.UpdatePosition -> TODO()
            is WorkoutListAction.TappedWorkout -> {
                emit(WorkoutListEffect.NavEditWorkout(action.workout.id))
            }
            WorkoutListAction.PlayAllTapped -> TODO()
            is WorkoutListAction.SinglePlayTapped -> TODO()
        }
    }

}