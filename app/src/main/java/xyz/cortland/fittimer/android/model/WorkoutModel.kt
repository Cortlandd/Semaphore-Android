package xyz.cortland.fittimer.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WorkoutModel(
    var seconds: Int?,
    var workoutName: String?): Parcelable {

    var id: Int? = 0
    var isPlaying: Boolean? = false
    var isCount: Boolean? = false
    var remainingSeconds: Long? = 0

    constructor(): this(null, null)

}
