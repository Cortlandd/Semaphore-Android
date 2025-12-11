package com.cortlandwalker.semaphore.core

import android.app.Application
import com.klipy.klipy_ui.KlipyUi
import com.klipy.sdk.KlipySdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val repo = KlipySdk.create(
            context = this,
            secretKey = "fNkmHZ257SEs5hOBeRF6XKSynwsVGodDUzMKzVBObkGgu2cb9vN0YDsHKh7ZyXQl",
            enableLogging = false
        )

        KlipyUi.configure(repo)
    }
}