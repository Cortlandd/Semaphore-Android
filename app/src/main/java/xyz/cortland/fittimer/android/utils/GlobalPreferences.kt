package xyz.cortland.fittimer.android.utils

import android.content.Context
import android.content.SharedPreferences
import xyz.cortland.fittimer.android.helpers.*
import java.util.*

class GlobalPreferences(context: Context) {

    var mSharedPreferences: SharedPreferences? = null

    init {
        mSharedPreferences = context.getSharedPreferences(GLOBAL_PREFERENCES, Context.MODE_PRIVATE)
    }

    /**
     *
     * Get the value indicating if a ActivityModel has been edited.
     * Set rather or not a ActivityModel has been edited.
     *
     * @return [Boolean]
     *
     */
    var activityModified: Boolean
        get() = mSharedPreferences!!.getBoolean(HAS_ACTIVITY_EDITED, false)
        set(value) = with(mSharedPreferences!!.edit()) { this.putBoolean(HAS_ACTIVITY_EDITED, value).apply() }

    /**
     *
     * Get value indicating rather or not the current image has been removed.
     * Set rather or not the current activityModel image has been removed.
     *
     * Set the text-to-speech language for the app.
     *
     * @return [Boolean]
     *
     */
    var currentImageRemoved: Boolean
        get() = mSharedPreferences!!.getBoolean(HAS_CURRENT_IMAGE_REMOVED, false)
        set(value) =  with(mSharedPreferences!!.edit()) { this.putBoolean(HAS_CURRENT_IMAGE_REMOVED, value).apply() }

    /**
     *
     *
     */
    var speechLanguage: String
        get() = mSharedPreferences!!.getString(CURRENT_LOCAL_LANGUAGE, Locale.getDefault().language)!!
        set(value) =  with(mSharedPreferences!!.edit()) { this.putString(CURRENT_LOCAL_LANGUAGE, value).apply() }

    /**
     *
     * Get the index of the currently playing ActivityModel from Playing All.
     * Set the index of the currently playing ActivityModel from Playing All.
     *
     * @return [Int]: The remaining seconds until a activityModel is complete
     *
     */
    var currentPlayingAllActivityPosition: Int
        get() = mSharedPreferences!!.getInt(CURRENT_PLAYING_ALL_ACTIVITY_POSITION, -1)
        set(position) = with(mSharedPreferences!!.edit()) { this.putInt(CURRENT_PLAYING_ALL_ACTIVITY_POSITION, position).apply() }

    /**
     * Get/Set the current activityModel, from Playing All's, remaining time.
     */
    var currentPlayingAllRemainingTime: Long
        get() = mSharedPreferences!!.getLong(CURRENT_PLAYING_ALL_ACTIVITY_REMAINING, 0)
        set(remainingTime) = with(mSharedPreferences!!.edit()) { this.putLong(CURRENT_PLAYING_ALL_ACTIVITY_REMAINING, remainingTime).apply() }

    /**
     * Get/Set rather a not the app is playing all activities vs a single activityModel
     *
     * @return [Boolean]: true or false rather the app is playing all activities or a single activityModel
     */
    var isPlayingAllActivities: Boolean
        get() = mSharedPreferences!!.getBoolean(IS_PLAYING_ALL_ACTIVITIES,false)
        set(isPlaying) = with(mSharedPreferences!!.edit()) { this.putBoolean(IS_PLAYING_ALL_ACTIVITIES, isPlaying).apply() }

    /**
     *
     * Method used to remove preferences
     *
     */
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