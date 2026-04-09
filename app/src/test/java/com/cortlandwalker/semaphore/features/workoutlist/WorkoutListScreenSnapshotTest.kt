package com.cortlandwalker.semaphore.features.workoutlist

import app.cash.paparazzi.Paparazzi
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import com.cortlandwalker.semaphore.playback.WorkoutPlaybackController
import com.cortlandwalker.semaphore.playback.WorkoutPlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test

class WorkoutListScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun `WorkoutListScreen empty state`() {
        paparazzi.snapshot {
            val reducer = WorkoutListReducer(
                InMemoryWorkoutRepository(emptyList()),
                SnapshotPlaybackController()
            )
            WorkoutListScreen(
                state = WorkoutListState(
                    workouts = emptyList(),
                    displayMode = ViewDisplayMode.Empty
                ),
                reducer = reducer
            )
        }
    }

    @Test
    fun `WorkoutListScreen loading state`() {
        paparazzi.snapshot {
            val reducer = WorkoutListReducer(
                InMemoryWorkoutRepository(emptyList()),
                SnapshotPlaybackController()
            )
            WorkoutListScreen(
                state = WorkoutListState(
                    workouts = emptyList(),
                    displayMode = ViewDisplayMode.Loading
                ),
                reducer = reducer
            )
        }
    }

    @Test
    fun `WorkoutListScreen content state`() {
        paparazzi.snapshot {
            val sample = listOf(
                Workout("1", 0, "Warm Up", "", 0, 2, 0, 0, 0),
                Workout("2", 0, "Push Ups", "", 0, 0, 33, 1, 0),
                Workout("3", 0, "High Knees", "", 0, 1, 0, 2, 0),
                Workout("4", 0, "Cool Down", "", 0, 5, 0, 3, 0),
            )
            val reducer = WorkoutListReducer(
                InMemoryWorkoutRepository(sample),
                SnapshotPlaybackController()
            )
            WorkoutListScreen(
                state = WorkoutListState(
                    workouts = sample,
                    displayMode = ViewDisplayMode.Content,
                    activeWorkoutId = "2",
                    activeWorkoutTimer = "00:00:24"
                ),
                reducer = reducer
            )
        }
    }

    private class SnapshotPlaybackController : WorkoutPlaybackController {
        private val state = MutableStateFlow(WorkoutPlaybackState())
        override val playbackState: StateFlow<WorkoutPlaybackState> = state.asStateFlow()

        override fun startSingle(workout: Workout) = Unit

        override fun startAll(workouts: List<Workout>) = Unit

        override fun stop() = Unit
    }
}
