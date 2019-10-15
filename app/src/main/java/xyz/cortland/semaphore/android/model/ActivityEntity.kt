package xyz.cortland.semaphore.android.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import xyz.cortland.semaphore.android.custom.CountDownTimer

@Entity
data class ActivityEntity (
    var hours: Int?,
    var minutes: Int?,
    var seconds: Int?,
    var activityName: String?,
    var activityImage: String?,
    var activitySpeech: Int?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var position: Int? = null
    var isDefaultState: Boolean? = true
    @Ignore var countDownTimer: CountDownTimer? = null

}