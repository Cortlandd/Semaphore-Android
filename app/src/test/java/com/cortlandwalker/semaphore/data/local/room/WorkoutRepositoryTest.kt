package com.cortlandwalker.semaphore.data.local.room

import com.cortlandwalker.semaphore.data.models.Workout
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WorkoutRepositoryTest {

    @Test
    fun `deleteById should delete the workout and its cached local image`() = runTest {
        val dao = mockk<WorkoutDao>(relaxed = true)
        val imageStore = mockk<WorkoutImageStore>(relaxed = true)
        val repo = RoomWorkoutRepository(dao, imageStore)
        val imageUri = "file:///data/user/0/com.cortlandwalker.semaphore/files/workout_media/1.gif"
        val workout = Workout(
            id = "1",
            createdAt = 0L,
            name = "Push Ups",
            imageUri = imageUri,
            hours = 0,
            minutes = 0,
            seconds = 30,
            position = 0,
            orderId = 0
        )

        coEvery { dao.getById("1") } returns workout

        repo.deleteById("1")

        coVerify {
            dao.getById("1")
            dao.delete("1")
            imageStore.deleteCachedLocalImage(imageUri)
        }
    }

    @Test
    fun `deleteById should skip media cleanup when there is no image`() = runTest {
        val dao = mockk<WorkoutDao>(relaxed = true)
        val imageStore = mockk<WorkoutImageStore>(relaxed = true)
        val repo = RoomWorkoutRepository(dao, imageStore)
        val workout = Workout(
            id = "1",
            createdAt = 0L,
            name = "Bodyweight",
            imageUri = null,
            hours = 0,
            minutes = 0,
            seconds = 30,
            position = 0,
            orderId = 0
        )

        coEvery { dao.getById("1") } returns workout

        repo.deleteById("1")

        coVerify { dao.delete("1") }
        coVerify(exactly = 0) { imageStore.deleteCachedLocalImage(any()) }
    }
}
