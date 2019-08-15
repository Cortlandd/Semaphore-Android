package xyz.cortland.fittimer.android.utils

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import xyz.cortland.fittimer.android.FitTimer
import java.util.*

class GlobalPreferences(context: Context) {

    var mSharedPreferences: SharedPreferences? = null

    val GLOBAL_PREFERENCES = "global_preferences"

    val HAS_WORKOUT_EDITED = "has_workout_edited"

    val HAS_CURRENT_IMAGE_REMOVED = "has_current_image_removed"

    val CURRENT_LOCAL_LANGUAGE = "current_local_language"

    init {
        mSharedPreferences = context.getSharedPreferences(GLOBAL_PREFERENCES, Context.MODE_PRIVATE)
    }

    /**
     *
     * Set rather or not a Workout has been edited
     *
     * @param edited: True or False if the Workout has been modified.
     *
     */
    fun setWorkoutModified(edited: Boolean) {
        with(mSharedPreferences!!.edit()) {
            this.putBoolean(HAS_WORKOUT_EDITED, edited)
            this.apply()
        }
    }

    /**
     *
     * Get the value rather the Workout has been edited
     *
     */
    fun isWorkoutModified(): Boolean {
        return mSharedPreferences!!.getBoolean(HAS_WORKOUT_EDITED, false)
    }

    /**
     *
     * Set rather or not the current workout image has been removed
     *
     */
    fun setCurrentImageRemoved(removed: Boolean) {
        with(mSharedPreferences!!.edit()) {
            this.putBoolean(HAS_CURRENT_IMAGE_REMOVED, removed)
            this.apply()
        }
    }

    /**
     *
     * Get value indicating rather or not the current image has been removed.
     *
     */
    fun isCurrentImageRemoved(): Boolean {
        return mSharedPreferences!!.getBoolean(HAS_CURRENT_IMAGE_REMOVED, false)
    }

    fun setSpeechLanguage(language: String) {
        with(mSharedPreferences!!.edit()) {
            this.putString(CURRENT_LOCAL_LANGUAGE, language)
            this.apply()
        }
    }

    fun getSpeechLanguage(): String {
        return mSharedPreferences!!.getString(CURRENT_LOCAL_LANGUAGE, Locale.getDefault().language)!!
    }

    /**
     *
     * Return global SharedPreferences variable in GlobalPreferences.
     *
     */
    fun getGlobalPreferences(): SharedPreferences? {
        return mSharedPreferences
    }

}