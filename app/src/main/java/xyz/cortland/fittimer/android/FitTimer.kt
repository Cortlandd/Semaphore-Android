package xyz.cortland.fittimer.android

import android.app.Application
import xyz.cortland.fittimer.android.utils.GlobalPreferences
import java.util.*

class FitTimer: Application() {

    // TODO: Potentially implement a shared AlertDialog.

    var preferences: GlobalPreferences? = null
    var availableLanguages = emptyArray<Locale>()

    init {
        instance = this
    }

    companion object {
        private var instance: FitTimer? = null

        fun applicationContext(): FitTimer {
            return instance as FitTimer
        }
    }

    override fun onCreate() {
        super.onCreate()

        preferences = GlobalPreferences(this)

        availableLanguages = Locale.getAvailableLocales()

    }

}