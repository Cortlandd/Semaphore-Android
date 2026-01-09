package com.cortlandwalker.semaphore.features.upsert

import app.cash.paparazzi.Paparazzi
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository
import com.cortlandwalker.semaphore.data.local.room.WorkoutImageStore
import org.junit.Rule
import org.junit.Test

class UpsertWorkoutScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun `UpsertWorkoutScreen initial state`() {
        paparazzi.snapshot {
            val dummyState = UpsertWorkoutState(
                viewDisplayMode = ViewDisplayMode.Content,
                workoutId = null,
                name = "",
                hours = 0,
                minutes = 29,
                seconds = 45,
                imageUri = null,
                error = null
            )

            val reducer = UpsertWorkoutReducer(
                InMemoryWorkoutRepository(),
                imageStore = WorkoutImageStore(paparazzi.context)
            )

            UpsertWorkoutScreen(
                state = dummyState,
                reducer = reducer
            )
        }
    }

    @Test
    fun `UpsertWorkoutScreen edit state`() {
        paparazzi.snapshot {
            val dummyState = UpsertWorkoutState(
                viewDisplayMode = ViewDisplayMode.Content,
                workoutId = "1",
                name = "Existing Workout",
                hours = 1,
                minutes = 15,
                seconds = 30,
                imageUri = "https://example.com/image.gif",
                error = null
            )

            val reducer = UpsertWorkoutReducer(
                InMemoryWorkoutRepository(),
                imageStore = WorkoutImageStore(paparazzi.context)
            )

            UpsertWorkoutScreen(
                state = dummyState,
                reducer = reducer
            )
        }
    }
}
