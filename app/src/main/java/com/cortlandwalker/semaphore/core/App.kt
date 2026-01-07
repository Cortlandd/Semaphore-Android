package com.cortlandwalker.semaphore.core

import android.app.Application
import com.klipy.klipy_ui.KlipyUi
import com.klipy.sdk.KlipySdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}