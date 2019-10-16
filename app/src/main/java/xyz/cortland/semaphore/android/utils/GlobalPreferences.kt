package xyz.cortland.semaphore.android.utils

import android.content.Context
import android.content.SharedPreferences
import xyz.cortland.semaphore.android.helpers.*
import java.util.*

class GlobalPreferences(context: Context) {

    var mSharedPreferences: SharedPreferences? = null

    init {
        mSharedPreferences = context.getSharedPreferences(GLOBAL_PREFERENCES, Context.MODE_PRIVATE)
    }

    /**
     *
     * Get value indicating rather or not the current image has been removed.
     * Set rather or not the current activityEntity image has been removed.
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
     * @return [Int]: The remaining seconds until a activityEntity is complete
     *
     */
    var currentPlayingAllActivityPosition: Int
        get() = mSharedPreferences!!.getInt(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION, -1)
        set(position) = with(mSharedPreferences!!.edit()) { this.putInt(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION, position).apply() }

    /**
     * Get/Set the current activityEntity, from Playing All's, remaining time.
     */
    var currentPlayingAllRemainingTime: Long
        get() = mSharedPreferences!!.getLong(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING, 0)
        set(remainingTime) = with(mSharedPreferences!!.edit()) { this.putLong(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING, remainingTime).apply() }

    /**
     * Get/Set rather a not the app is playing all activities vs a single activityEntity
     *
     * @return [Boolean]: true or false rather the app is playing all activities or a single activityEntity
     */
    var isPlayingAllActivities: Boolean
        get() = mSharedPreferences!!.getBoolean(IS_PLAYING_ALL_ACTIVITIES,false)
        set(isPlaying) = with(mSharedPreferences!!.edit()) { this.putBoolean(IS_PLAYING_ALL_ACTIVITIES, isPlaying).apply() }

    /**
     * Get/Set rather or not you're editing an Activity from options menu
     *
     * @return [Boolean]: true or false
     */
    var isOptionEditingActivity: Boolean
        get() = mSharedPreferences!!.getBoolean(IS_OPTION_EDITING_ACTIVITY, false)
        set(isEditing) = with(mSharedPreferences!!.edit()) { this.putBoolean(IS_OPTION_EDITING_ACTIVITY, isEditing).apply() }

    /**
     * Get/Set the int of the selected Activity to be edited from the options menu.
     *
     * @return [Int]: selected Activity Id
     */
    var optionEditSelectedActivityId: Int
        get() = mSharedPreferences!!.getInt(OPTION_EDITING_ACTIVITY_ID, 0)
        set(activityId) = with(mSharedPreferences!!.edit()) { this.putInt(OPTION_EDITING_ACTIVITY_ID, activityId).apply() }

    /**
     * Get/Set rather or not ActivityFragment is in the Foreground.
     *
     * @return [Boolean]: is ActivityFragment in the Foreground or not?
     */
    var isActivityFragmentForeground: Boolean
        get() = mSharedPreferences!!.getBoolean(IS_ACTIVITY_FRAGMENT_FOREGROUND, true)
        set(isForeground) = with(mSharedPreferences!!.edit()) { this.putBoolean(IS_ACTIVITY_FRAGMENT_FOREGROUND, isForeground).apply() }

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
     * Get/Set rather or not the app is playing all activities in order
     *
     * @return [Boolean]: true or false rather the app is playing all activities in order or not
     */
    var isPlayingAllInOrderActivities: Boolean
        get() = mSharedPreferences!!.getBoolean(IS_PLAYING_ALL_IN_ORDER_ACTIVITIES, false)
        set(isPlayingAllInOrder) = with(mSharedPreferences!!.edit()) { this.putBoolean(IS_PLAYING_ALL_IN_ORDER_ACTIVITIES, isPlayingAllInOrder).apply() }

    var totalActivitiesHours: Int?
        get() = mSharedPreferences!!.getInt(ACTIVITIES_TOTAL_HOURS, 0)
        set(totalHours) = with(mSharedPreferences!!.edit()) { this.putInt(ACTIVITIES_TOTAL_HOURS, totalHours!!).apply() }

    var totalActivitiesMinutes: Int?
        get() = mSharedPreferences!!.getInt(ACTIVITIES_TOTAL_MINUTES, 0)
        set(totalMinutes) = with(mSharedPreferences!!.edit()) { this.putInt(ACTIVITIES_TOTAL_MINUTES, totalMinutes!!).apply() }

    var totalActivitiesSeconds: Int?
        get() = mSharedPreferences!!.getInt(ACTIVITIES_TOTAL_SECONDS, 0)
        set(totalSeconds) = with(mSharedPreferences!!.edit()) { this.putInt(ACTIVITIES_TOTAL_SECONDS, totalSeconds!!).apply() }

    /**
     *
     * Return global SharedPreferences variable in GlobalPreferences.
     *
     */
    fun getGlobalPreferences(): SharedPreferences? {
        return mSharedPreferences
    }

}