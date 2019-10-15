package xyz.cortland.semaphore.android.helpers

import xyz.cortland.semaphore.android.SemaphoreApp

const val ACTIVITY_FINISHED_ID = 9000
const val ACTIVITY_CHANNEL = "activity_channel_9000"
const val IMAGE_PICK_CODE = 1000
/* Related to SharedPreferences */
const val GLOBAL_PREFERENCES = "global_preferences"
const val HAS_ACTIVITY_EDITED = "has_activity_edited"
const val HAS_CURRENT_IMAGE_REMOVED = "has_current_image_removed"
const val CURRENT_LOCAL_LANGUAGE = "current_local_language"
const val CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION = "current_playing_all_activity_position"
const val CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING = "current_playing_all_activity_remaining"
const val IS_PLAYING_ALL_ACTIVITIES = "is_playing_all_activities"
const val IS_PLAYING_ALL_IN_ORDER_ACTIVITIES = "is_playing_all_in_order_activities"
const val IS_OPTION_EDITING_ACTIVITY = "is_option_editing_activity"
const val OPTION_EDITING_ACTIVITY_ID = "option_editing_activity_id"
const val IS_ACTIVITY_FRAGMENT_FOREGROUND = "is_activity_fragment_foreground"
val prefs = SemaphoreApp.applicationContext().preferences!!