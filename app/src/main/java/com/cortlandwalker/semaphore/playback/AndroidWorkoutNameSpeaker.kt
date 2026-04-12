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

    private val pendingTexts = ArrayDeque<String>()

    override fun prepare() {
        ensureInitialized()
    }

    override fun speak(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        prepare()

        val engine = tts ?: return
        if (isReady) {
            speakNow(engine, trimmedName, TextToSpeech.QUEUE_FLUSH)
        } else {
            synchronized(pendingTexts) {
                pendingTexts.addLast(trimmedName)
            }
        }
    }

    override fun stop() {
        synchronized(pendingTexts) {
            pendingTexts.clear()
        }
        tts?.stop()
    }

    override fun onInit(status: Int) {
        val engine = tts ?: return
        if (status != TextToSpeech.SUCCESS) return

        engine.language = Locale.getDefault()
        isReady = true

        synchronized(pendingTexts) {
            var queueMode = TextToSpeech.QUEUE_FLUSH
            while (pendingTexts.isNotEmpty()) {
                speakNow(engine, pendingTexts.removeFirst(), queueMode)
                queueMode = TextToSpeech.QUEUE_ADD
            }
        }
    }

    @Synchronized
    private fun ensureInitialized() {
        if (tts != null) return
        tts = TextToSpeech(appContext, this)
    }

    private fun speakNow(engine: TextToSpeech, text: String, queueMode: Int) {
        engine.speak(text, queueMode, null, "workout_name")
    }
}
