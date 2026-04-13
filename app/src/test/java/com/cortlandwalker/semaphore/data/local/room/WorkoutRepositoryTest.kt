package com.cortlandwalker.semaphore.data.local.room

import com.cortlandwalker.semaphore.data.models.Workout
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WorkoutRepositoryTest {

    @Test
    fun `deleteById should delete a cached local image when no workouts still reference it`() = runTest {
        val dao = mockk<WorkoutDao>(relaxed = true)
        val imageStore = mockk<WorkoutImageStore>(relaxed = true)
        val repo = RoomWorkoutRepository(dao, imageStore)
        val imageUri = "file:///data/user/0/com.cortlandwalker.semaphore/files/workout_media/pushups.gif"
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
        coEvery { dao.countByImageUri(imageUri) } returns 0

        repo.deleteById("1")

        coVerify(ordering = io.mockk.Ordering.SEQUENCE) {
            dao.getById("1")
            dao.delete("1")
            dao.countByImageUri(imageUri)
            imageStore.deleteCachedLocalImage(imageUri)
        }
    }

    @Test
    fun `deleteById should keep a cached local image when another workout still references it`() = runTest {
        val dao = mockk<WorkoutDao>(relaxed = true)
        val imageStore = mockk<WorkoutImageStore>(relaxed = true)
        val repo = RoomWorkoutRepository(dao, imageStore)
        val imageUri = "file:///data/user/0/com.cortlandwalker.semaphore/files/workout_media/shared.gif"
        val workout = Workout(
            id = "1",
            createdAt = 0L,
            name = "Shared GIF",
            imageUri = imageUri,
            hours = 0,
            minutes = 0,
            seconds = 30,
            position = 0,
            orderId = 0
        )

        coEvery { dao.getById("1") } returns workout
        coEvery { dao.countByImageUri(imageUri) } returns 1

        repo.deleteById("1")

        coVerify { dao.delete("1") }
        coVerify(exactly = 0) { imageStore.deleteCachedLocalImage(any()) }
    }
}
