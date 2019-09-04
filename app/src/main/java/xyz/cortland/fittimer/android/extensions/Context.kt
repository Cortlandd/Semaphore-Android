package xyz.cortland.fittimer.android.extensions

import android.content.Context
import android.speech.tts.TextToSpeech
import xyz.cortland.fittimer.android.FitTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.utils.GlobalPreferences

fun Context.speakText(text: String) {
    FitTimer.applicationContext().textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
}

val Context.dbHandler: WorkoutDatabase get() = WorkoutDatabase(applicationContext, null)