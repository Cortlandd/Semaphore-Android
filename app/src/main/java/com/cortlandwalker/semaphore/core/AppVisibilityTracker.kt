package com.cortlandwalker.semaphore.core

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

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
