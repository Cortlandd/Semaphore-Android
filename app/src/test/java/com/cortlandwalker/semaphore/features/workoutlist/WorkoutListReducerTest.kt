package com.cortlandwalker.semaphore.features.workoutlist

import com.cortlandwalker.ghettoxide.testBind
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WorkoutListReducerTest {

    private lateinit var mockRepo: WorkoutRepository
    private lateinit var reducer: WorkoutListReducer
    private lateinit var effects: MutableList<WorkoutListEffect>

    @Before
    fun setUp() {
        mockRepo = mockk(relaxed = true)
        effects = mutableListOf<WorkoutListEffect>()
        reducer = WorkoutListReducer(mockRepo)
        reducer.testBind(
            initialState = WorkoutListState(),
            effects = effects
        )
    }

    // Working on it
    @Test
    fun `OnLoad should collect workouts and update state`() = runTest {
        // Given
        val workouts = listOf(
            Workout("1", 0, "Test Workout", "", 0, 1, 0, 0, 0)
        )
        val postedActions = mutableListOf<WorkoutListAction>(WorkoutListAction.OnLoad)
        coEvery { mockRepo.observeAllOrderedByPosition() } returns flowOf(workouts)

        // When
        reducer.testBind(
            initialState = WorkoutListState(),
            effects = effects,
            postedActions = postedActions,
            scope = this
        )

        reducer.onLoadAction()?.let { action ->
            reducer.accept(action)
        }

        // Then
        val state = reducer.currentState
        assertThat(state.workouts).isEqualTo(workouts)
    }

    @Test
    fun `TappedSettings should emit NavSettings effect`() = runTest {
        // When
        reducer.accept(WorkoutListAction.TappedSettings)

        // Then
        assertThat(effects).contains(WorkoutListEffect.NavSettings)
    }

    @Test
    fun `TappedAddWorkout should emit NavAddWorkout effect`() = runTest {
        // When
        reducer.accept(WorkoutListAction.TappedAddWorkout)

        // Then
        assertThat(effects).contains(WorkoutListEffect.NavAddWorkout)
    }

    @Test
    fun `DeleteTapped should delete workout from repo`() = runTest {
        // Given
        val workoutId = "1"

        // When
        reducer.accept(WorkoutListAction.DeleteTapped(workoutId))

        // Then
        coVerify { mockRepo.deleteById(workoutId) }
    }

    @Test
    fun `TappedWorkout should emit NavEditWorkout effect`() = runTest {
        // Given
        val workout = Workout("1", 0, "Test Workout", "", 0, 1, 0, 0, 0)

        // When
        reducer.accept(WorkoutListAction.TappedWorkout(workout))

        // Then
        assertThat(effects).contains(WorkoutListEffect.NavEditWorkout(workout.id))
    }
}
