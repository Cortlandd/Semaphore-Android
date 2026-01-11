package com.cortlandwalker.semaphore.features.upsert

import com.cortlandwalker.ghettoxide.testBind
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.WorkoutImageStore
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpsertWorkoutReducerTest {

    private lateinit var mockRepo: WorkoutRepository
    private lateinit var mockImageStore: WorkoutImageStore
    private lateinit var reducer: UpsertWorkoutReducer
    private lateinit var effects: MutableList<UpsertWorkoutEffect>

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepo = mockk(relaxed = true)
        mockImageStore = mockk(relaxed = true)
        effects = mutableListOf()
        reducer = UpsertWorkoutReducer(mockRepo, mockImageStore)
        reducer.testBind(
            initialState = UpsertWorkoutState(),
            effects = effects
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Init with null workoutId should set displayMode to Empty`() = runTest {
        // When
        reducer.accept(UpsertWorkoutAction.Init(null))

        // Then
        val state = reducer.currentState
        assertThat(state.viewDisplayMode).isEqualTo(ViewDisplayMode.Empty)
        assertThat(state.workoutId).isNull()
    }

    @Test
    fun `Init with workoutId should load workout and set displayMode to Content`() = runTest {
        // Given
        val workoutId = "1"
        val workout = Workout(workoutId, 0, "Test Workout", "", 1, 2, 3, 0, 0)
        coEvery { mockRepo.getById(workoutId) } returns workout

        // When
        reducer.accept(UpsertWorkoutAction.Init(workoutId))

        // Then
        val state = reducer.currentState
        assertThat(state.viewDisplayMode).isEqualTo(ViewDisplayMode.Content)
        assertThat(state.name).isEqualTo(workout.name)
        assertThat(state.hours).isEqualTo(workout.hours)
        assertThat(state.minutes).isEqualTo(workout.minutes)
        assertThat(state.seconds).isEqualTo(workout.seconds)
    }

    @Test
    fun `NameChanged should update the name in the state`() = runTest {
        // Given
        val newName = "New Workout Name"

        // When
        reducer.accept(UpsertWorkoutAction.NameChanged(newName))

        // Then
        val state = reducer.currentState
        assertThat(state.name).isEqualTo(newName)
    }

    @Test
    fun `TimeSet should update the time in the state`() = runTest {
        // Given
        val hours = 1
        val minutes = 30
        val seconds = 45

        // When
        reducer.accept(UpsertWorkoutAction.TimeSet(hours, minutes, seconds))

        // Then
        val state = reducer.currentState
        assertThat(state.hours).isEqualTo(hours)
        assertThat(state.minutes).isEqualTo(minutes)
        assertThat(state.seconds).isEqualTo(seconds)
    }

    @Test
    fun `SaveClicked with blank name should show error`() = runTest {
        // When
        reducer.accept(UpsertWorkoutAction.SaveClicked)

        // Then
        assertThat(effects.first()).isInstanceOf(UpsertWorkoutEffect.ShowError::class.java)
    }

    @Test
    fun `SaveClicked with new workout should insert into repo and go back`() = runTest {
        // Given
        reducer.accept(UpsertWorkoutAction.NameChanged("New Workout"))
        reducer.accept(UpsertWorkoutAction.TimeSet(0, 1, 0))

        // When
        reducer.accept(UpsertWorkoutAction.SaveClicked)

        // Then
        assertThat(effects).contains(UpsertWorkoutEffect.Back)
        coVerify { mockRepo.insert(any()) }
    }
}
