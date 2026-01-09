package com.cortlandwalker.semaphore.features.workoutlist

import app.cash.paparazzi.Paparazzi
import com.cortlandwalker.semaphore.data.models.Workout
import org.junit.Rule
import org.junit.Test

class WorkoutListRowSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun `WorkoutRow expanded with image`() {
        paparazzi.snapshot {
            val workoutWithImage = Workout(
                id = "1",
                createdAt = 0,
                name = "Workout With Image",
                imageUri = "https://static.klipy.com/ii/2711dd8a75a85be822d136ec94899b3f/40/01/vT7gcxyy.gif",
                hours = 0,
                minutes = 1,
                seconds = 30,
                position = 0,
                orderId = 0
            )
            WorkoutRow(
                workout = workoutWithImage,
                isExpanded = true,
                onPlayClicked = {},
                onClick = {},
                activeProgress = "00:45"
            )
        }
    }
}
