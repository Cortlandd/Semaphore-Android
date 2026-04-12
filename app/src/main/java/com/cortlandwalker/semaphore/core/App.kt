package com.cortlandwalker.semaphore.core

import android.app.Application
import com.cortlandwalker.semaphore.BuildConfig
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.playback.WorkoutNameSpeaker
import com.klipy.klipy_ui.KlipyUi
import com.klipy.sdk.KlipySdk
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application entry point for Semaphore.
 *
 * This is where app-wide services are initialized before any screen is shown,
 * including visibility tracking, AdMob, and the shared Klipy SDK instance.
 */
@HiltAndroidApp
class App : Application() {
    @Inject lateinit var workoutRepository: WorkoutRepository
    @Inject lateinit var workoutNameSpeaker: WorkoutNameSpeaker

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        AppVisibilityTracker.initialize()
        MobileAds.initialize(this)
        appScope.launch {
            if (workoutRepository.hasAnySpokenWorkouts()) {
                workoutNameSpeaker.prepare()
            }
        }

        val klipyApiKey = KlipyConfig.resolvedApiKey()
        if (klipyApiKey.isNotBlank()) {
            KlipyUi.configure {
                KlipySdk.create(
                    context = this,
                    secretKey = klipyApiKey,
                    enableLogging = BuildConfig.DEBUG
                )
            }
        }
    }
}
