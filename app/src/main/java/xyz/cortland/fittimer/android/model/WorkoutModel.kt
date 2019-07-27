package xyz.cortland.fittimer.android.model

import android.os.Parcelable

data class WorkoutModel(
    var seconds: Int?,
    var workoutName: String?) {

    var isPlaying: Boolean? = false
    var isCount: Boolean? = false
    var remainingSeconds: Long? = 0

    constructor(): this(null, null)

}
