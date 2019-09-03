package xyz.cortland.fittimer.android.helpers

import xyz.cortland.fittimer.android.FitTimer

const val WORKOUT_FINISHED_ID = 9000
const val WORKOUT_CHANNEL = "workout_channel_9000"
const val IMAGE_PICK_CODE = 1000
/* Related to SharedPreferences */
const val GLOBAL_PREFERENCES = "global_preferences"
const val HAS_WORKOUT_EDITED = "has_workout_edited"
const val HAS_CURRENT_IMAGE_REMOVED = "has_current_image_removed"
const val CURRENT_LOCAL_LANGUAGE = "current_local_language"
const val CURRENT_PLAYING_ALL_WORKOUT_POSITION = "current_playing_all_workout_position"
const val CURRENT_PLAYING_ALL_WORKOUT_REMAINING = "current_playing_all_workout_remaining"
const val EDITING_WORKOUT = "editing_workout"
const val LONGPRESS_WORKOUT_ID = "longpress_workout_id"
val prefs = FitTimer.applicationContext().preferences