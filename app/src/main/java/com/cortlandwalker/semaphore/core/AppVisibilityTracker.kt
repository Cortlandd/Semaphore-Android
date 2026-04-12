package com.cortlandwalker.semaphore.core

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Tracks whether the Semaphore process is currently visible to the user.
 *
 * Playback and notification code uses this shared observer to decide when
 * foreground-only UI should handle events directly versus when background
 * alerts should be shown to bring the user back into the app.
 */
object AppVisibilityTracker : DefaultLifecycleObserver {

    @Volatile
    private var isInForeground = false

    fun initialize() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun isAppVisible(): Boolean = isInForeground

    override fun onStart(owner: LifecycleOwner) {
        isInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isInForeground = false
    }
}
