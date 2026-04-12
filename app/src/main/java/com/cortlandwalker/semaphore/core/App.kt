package com.cortlandwalker.semaphore.core

import android.app.Application
import com.cortlandwalker.semaphore.BuildConfig
import com.klipy.klipy_ui.KlipyUi
import com.klipy.sdk.KlipySdk
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for Semaphore.
 *
 * This is where app-wide services are initialized before any screen is shown,
 * including visibility tracking, AdMob, and the shared Klipy SDK instance.
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppVisibilityTracker.initialize()
        MobileAds.initialize(this)

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
