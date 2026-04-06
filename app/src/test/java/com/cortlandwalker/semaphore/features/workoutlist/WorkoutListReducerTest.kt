package com.cortlandwalker.semaphore.features.workoutlist

import com.cortlandwalker.ghettoxide.testBind
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class WorkoutListReducerTest {

    private lateinit var mockRepo: WorkoutRepository
    private lateinit var reducer: WorkoutListReducer
    private lateinit var effects: MutableList<WorkoutListEffect>

    // Working on it
    @Test
    fun `OnLoad should collect workouts and update state`() = runTest {
        val workouts = listOf(
            Workout("1", 0, "Test Workout", "", 0, 1, 0, 0, 0)
        )
        val repo = InMemoryWorkoutRepository(workouts)
        reducer = WorkoutListReducer(repo, 1_000L)
        effects = mutableListOf()
        reducer.testBind(
            initialState = WorkoutListState(),
            effects = effects,
            scope = backgroundScope
        )

        reducer.accept(WorkoutListAction.OnLoad)
        runCurrent()

        assertThat(reducer.currentState.workouts).isEqualTo(workouts)
    }

    @Test
    fun `TappedSettings should emit NavSettings effect`() = runTest {
        setUpReducer()

        // When
        reducer.accept(WorkoutListAction.TappedSettings)

        // Then
        assertThat(effects).contains(WorkoutListEffect.NavSettings)
    }

    @Test
    fun `TappedAddWorkout should emit NavAddWorkout effect`() = runTest {
        setUpReducer()

        // When
        reducer.accept(WorkoutListAction.TappedAddWorkout)

        // Then
        assertThat(effects).contains(WorkoutListEffect.NavAddWorkout)
    }

    @Test
    fun `DeleteTapped should delete workout from repo`() = runTest {
        setUpReducer()

        // Given
        val workoutId = "1"

        // When
        reducer.accept(WorkoutListAction.DeleteTapped(workoutId))

        // Then
        coVerify { mockRepo.deleteById(workoutId) }
    }

    @Test
    fun `TappedWorkout should emit NavEditWorkout effect`() = runTest {
        setUpReducer()

        // Given
        val workout = Workout("1", 0, "Test Workout", "", 0, 1, 0, 0, 0)

        // When
        reducer.accept(WorkoutListAction.TappedWorkout(workout))

        // Then
        assertThat(effects).contains(WorkoutListEffect.NavEditWorkout(workout.id))
    }

    @Test
    fun `SinglePlayTapped should count down and update workout analytics`() = runTest {
        val workout = Workout("1", 0, "Plank", "", 0, 0, 2, 0, 0)
        val repo = InMemoryWorkoutRepository(listOf(workout))

        reducer = WorkoutListReducer(repo, 1_000L)
        effects = mutableListOf()
        reducer.testBind(
            initialState = WorkoutListState(
                workouts = listOf(workout),
                displayMode = ViewDisplayMode.Content
            ),
            effects = effects,
            scope = this
        )

        reducer.accept(WorkoutListAction.SinglePlayTapped(workout.id))
        runCurrent()

        assertThat(reducer.currentState.activeWorkoutId).isEqualTo(workout.id)
        assertThat(reducer.currentState.activeWorkoutTimer).isEqualTo("00:02")

        advanceTimeBy(2_000L)
        runCurrent()

        assertThat(reducer.currentState.activeWorkoutId).isNull()
        assertThat(reducer.currentState.activeWorkoutTimer).isNull()
        assertThat(reducer.currentState.isPlayingAll).isFalse()

        val updatedWorkout = repo.getById(workout.id)
        assertThat(updatedWorkout).isNotNull()
        assertThat(updatedWorkout?.completedCount).isEqualTo(1)
        assertThat(updatedWorkout?.totalTimeSpentSeconds).isEqualTo(2L)
        assertThat(updatedWorkout?.currentStreak).isEqualTo(1)
        assertThat(updatedWorkout?.lastPerformedAt).isNotNull()
    }

    @Test
    fun `PlayAllTapped should advance through every workout and clear playback state`() = runTest {
        val workouts = listOf(
            Workout("1", 0, "Warm Up", "", 0, 0, 1, 0, 0),
            Workout("2", 0, "Push Ups", "", 0, 0, 1, 1, 0)
        )
        val repo = InMemoryWorkoutRepository(workouts)

        reducer = WorkoutListReducer(repo, 1_000L)
        effects = mutableListOf()
        reducer.testBind(
            initialState = WorkoutListState(
                workouts = workouts,
                displayMode = ViewDisplayMode.Content
            ),
            effects = effects,
            scope = this
        )

        reducer.accept(WorkoutListAction.PlayAllTapped)
        runCurrent()

        assertThat(reducer.currentState.activeWorkoutId).isEqualTo("1")
        assertThat(reducer.currentState.isPlayingAll).isTrue()
        assertThat(reducer.currentState.playbackQueue).containsExactly("2")

        advanceTimeBy(1_000L)
        runCurrent()

        assertThat(reducer.currentState.activeWorkoutId).isEqualTo("2")
        assertThat(reducer.currentState.activeWorkoutTimer).isEqualTo("00:01")
        assertThat(reducer.currentState.playbackQueue).isEmpty()

        advanceTimeBy(1_000L)
        runCurrent()

        assertThat(reducer.currentState.activeWorkoutId).isNull()
        assertThat(reducer.currentState.activeWorkoutTimer).isNull()
        assertThat(reducer.currentState.isPlayingAll).isFalse()
        assertThat(reducer.currentState.playbackQueue).isEmpty()

        assertThat(repo.getById("1")?.completedCount).isEqualTo(1)
        assertThat(repo.getById("2")?.completedCount).isEqualTo(1)
    }

    private fun setUpReducer(
        initialState: WorkoutListState = WorkoutListState(),
        scope: CoroutineScope? = null
    ) {
        mockRepo = mockk(relaxed = true)
        effects = mutableListOf()
        reducer = WorkoutListReducer(mockRepo, 1_000L)
        reducer.testBind(
            initialState = initialState,
            effects = effects,
            scope = scope
        )
    }
}
