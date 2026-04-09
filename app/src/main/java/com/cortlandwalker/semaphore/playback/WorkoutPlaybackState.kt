package com.cortlandwalker.semaphore.playback

data class WorkoutPlaybackState(
    val isRunning: Boolean = false,
    val isPlayingAll: Boolean = false,
    val activeWorkoutId: String? = null,
    val activeWorkoutName: String? = null,
    val activeWorkoutImageUri: String? = null,
    val activeWorkoutTimer: String? = null,
    val remainingSeconds: Int? = null,
    val durationSeconds: Int? = null,
    val playbackQueue: List<String> = emptyList()
)
