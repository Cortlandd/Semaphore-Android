package xyz.cortland.fittimer.android.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.greenrobot.eventbus.EventBus
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.helpers.WORKOUT_CHANNEL
import xyz.cortland.fittimer.android.helpers.WORKOUT_FINISHED_ID
import xyz.cortland.fittimer.android.model.Workout

class WorkoutPlaybackReceiver : BroadcastReceiver() {

    var countdown: CountDownTimer? = null

    override fun onReceive(context: Context, intent: Intent) {

        val action = intent.action

        /**
         * Get stickyEvent from EventBus class for CountDownTimer
         */
        val stickyEvent = EventBus.getDefault().getStickyEvent(CountDownEvent::class.java)
        if (stickyEvent != null) {
            countdown = stickyEvent.countdownTimer

            when (action) {
                "workout.stop" -> {
                    if (countdown != null) {
                        println("Stopped Workout")
                        countdown?.cancel()
                    }
                }
                "workout.pause" -> {
                    if (countdown != null) {
                        println("Paused Workout")
                        countdown?.pause()

                    }
                }
                "workout.resume" -> {
                    if (countdown != null) {
                        println("Resumed Workout")
                        countdown?.resume()
                    }
                }
            }

        } else {
            return
        }
    }
}
