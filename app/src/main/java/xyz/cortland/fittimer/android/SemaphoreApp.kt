package xyz.cortland.fittimer.android

import android.app.Application
import android.speech.tts.TextToSpeech
import xyz.cortland.fittimer.android.utils.GlobalPreferences
import java.util.*

class SemaphoreApp: Application() {

    // TODO: Potentially implement a shared AlertDialog.

    var preferences: GlobalPreferences? = null
    var availableLanguages = emptyArray<Locale>()
    var textToSpeech: TextToSpeech? = null

    init {
        instance = this
    }

    companion object {
        private var instance: SemaphoreApp? = null

        fun applicationContext(): SemaphoreApp {
            return instance as SemaphoreApp
        }
    }

    override fun onCreate() {
        super.onCreate()

        preferences = GlobalPreferences(this)

        availableLanguages = Locale.getAvailableLocales()

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val language = textToSpeech?.setLanguage(Locale(preferences?.speechLanguage))
                if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language Not Supported.")
                } else {
                    println("Language Supported.")
                }
            }
        })

    }

}