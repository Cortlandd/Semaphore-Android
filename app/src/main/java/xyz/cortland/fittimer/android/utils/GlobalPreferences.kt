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

    val CURRENT_PLAYING_ALL_WORKOUT_POSITION = "current_playing_all_workout_position"

    val CURRENT_PLAYING_ALL_WORKOUT_REMAINING = "current_playing_all_workout_remaining"

    init {
        mSharedPreferences = context.getSharedPreferences(GLOBAL_PREFERENCES, Context.MODE_PRIVATE)
    }

    /**
     *
     * Set rather or not a Workout has been edited.
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
     * Get the value indicating if a Workout has been edited.
     *
     * @return [Boolean]
     *
     */
    fun isWorkoutModified(): Boolean {
        return mSharedPreferences!!.getBoolean(HAS_WORKOUT_EDITED, false)
    }



    /**
     *
     * Set rather or not the current workout image has been removed.
     *
     * @param removed [Boolean]
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
     * @return [Boolean]
     *
     */
    fun isCurrentImageRemoved(): Boolean {
        return mSharedPreferences!!.getBoolean(HAS_CURRENT_IMAGE_REMOVED, false)
    }

    /**
     *
     * Set the text-to-speech language for the app.
     *
     * @param language: The chosen language
     *
     */
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
     * Get the index of the currently playing Workout from Playing All.
     *
     * @return [Int]: The remaining seconds until a workout is complete
     *
     */
    fun getCurrentPlayingAllWorkoutPosition(): Int {
        return mSharedPreferences!!.getInt(CURRENT_PLAYING_ALL_WORKOUT_POSITION, -1)
    }

    /**
     *
     * Set the index of the currently playing Workout from Playing All.
     *
     * @param position: The position/index of the Workout that's currently playing
     *
     */
    fun setCurrentPlayingAllWorkoutPosition(position: Int) {
        with(mSharedPreferences!!.edit()) {
            this.putInt(CURRENT_PLAYING_ALL_WORKOUT_POSITION, position)
            this.apply()
        }
    }

    fun getCurrentPlayingAllRemainingTime(): Int {
        return mSharedPreferences!!.getInt(CURRENT_PLAYING_ALL_WORKOUT_REMAINING, 0)
    }

    fun setCurrentPlayingAllRemainingTime(remainingTime: Int) {
        with(mSharedPreferences!!.edit()) {
            this.putInt(CURRENT_PLAYING_ALL_WORKOUT_REMAINING, remainingTime)
            this.apply()
        }
    }

    fun removePreferences(key: String) {
        with(mSharedPreferences!!.edit()) {
            this.remove(key)
            this.apply()
        }
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