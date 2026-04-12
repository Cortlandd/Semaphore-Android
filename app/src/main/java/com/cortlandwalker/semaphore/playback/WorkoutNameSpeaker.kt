package com.cortlandwalker.semaphore.playback

interface WorkoutNameSpeaker {
    fun prepare()
    fun speak(name: String)
    fun stop()
}

object NoOpWorkoutNameSpeaker : WorkoutNameSpeaker {
    override fun prepare() = Unit
    override fun speak(name: String) = Unit
    override fun stop() = Unit
}
