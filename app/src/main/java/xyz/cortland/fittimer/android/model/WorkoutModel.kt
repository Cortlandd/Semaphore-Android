package xyz.cortland.fittimer.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WorkoutModel(var seconds: Int?, var workoutName: String?, var workoutImage: String?): Parcelable {

    var id: Int? = null
    var isPlaying: Boolean? = false
    var isCount: Boolean? = false
    var remainingSeconds: Long? = 0
    var expanded: Boolean? = false
    var isDefaultState: Boolean? = true

    constructor(): this(null, null, null)

}
