package com.cortlandwalker.semaphore.playback

import com.cortlandwalker.semaphore.data.models.Workout
import kotlinx.coroutines.flow.StateFlow

interface WorkoutPlaybackController {
    val playbackState: StateFlow<WorkoutPlaybackState>

    fun startSingle(workout: Workout)

    fun startAll(workouts: List<Workout>)

    fun pause()

    fun resume()

    fun stop()
}
