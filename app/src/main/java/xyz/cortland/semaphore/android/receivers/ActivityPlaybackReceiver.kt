package xyz.cortland.semaphore.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.greenrobot.eventbus.EventBus
import xyz.cortland.semaphore.android.custom.CountDownTimer

class ActivityPlaybackReceiver : BroadcastReceiver() {

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
                "activityModel.stop" -> {
                    if (countdown != null) {
                        println("Stopped ActivityModel")
                        countdown?.cancel()
                    }
                }
                "activityModel.pause" -> {
                    if (countdown != null) {
                        println("Paused ActivityModel")
                        countdown?.pause()

                    }
                }
                "activityModel.resume" -> {
                    if (countdown != null) {
                        println("Resumed ActivityModel")
                        countdown?.resume()
                    }
                }
            }

        } else {
            return
        }
    }
}
