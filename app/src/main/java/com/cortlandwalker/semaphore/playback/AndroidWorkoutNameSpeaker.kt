package com.cortlandwalker.semaphore.playback

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidWorkoutNameSpeaker @Inject constructor(
    @ApplicationContext private val appContext: Context
) : WorkoutNameSpeaker, TextToSpeech.OnInitListener {

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var isReady = false

    @Volatile
    private var pendingText: String? = null

    override fun speak(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        ensureInitialized()

        val engine = tts ?: return
        if (isReady) {
            speakNow(engine, trimmedName)
        } else {
            pendingText = trimmedName
        }
    }

    override fun stop() {
        pendingText = null
        tts?.stop()
    }

    override fun onInit(status: Int) {
        val engine = tts ?: return
        if (status != TextToSpeech.SUCCESS) return

        engine.language = Locale.getDefault()
        isReady = true

        pendingText?.let {
            speakNow(engine, it)
            pendingText = null
        }
    }

    @Synchronized
    private fun ensureInitialized() {
        if (tts != null) return
        tts = TextToSpeech(appContext, this)
    }

    private fun speakNow(engine: TextToSpeech, text: String) {
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "workout_name")
    }
}
