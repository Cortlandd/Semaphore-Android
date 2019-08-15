package xyz.cortland.fittimer.android

import android.app.Application
import android.speech.tts.TextToSpeech
import xyz.cortland.fittimer.android.utils.GlobalPreferences
import java.util.*

class FitTimer: Application() {

    var mGlobalPreferences: GlobalPreferences? = null
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

        mGlobalPreferences = GlobalPreferences(this)

        availableLanguages = Locale.getAvailableLocales()

    }

}