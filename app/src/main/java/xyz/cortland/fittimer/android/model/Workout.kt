package xyz.cortland.fittimer.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import xyz.cortland.fittimer.android.custom.CountDownTimer

@Parcelize
data class Workout(var seconds: Int?, var workoutName: String?, var workoutImage: String?, var workoutSpeech: Int?): Parcelable {

    var id: Int? = null
    var isPlaying: Boolean? = false
    var isDefaultState: Boolean? = true
    var countDownTimer: CountDownTimer? = null

    constructor(): this(null, null, null, null)

}
