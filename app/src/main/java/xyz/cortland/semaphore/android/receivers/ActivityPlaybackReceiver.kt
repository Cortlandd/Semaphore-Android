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
                "activityEntity.stop" -> {
                    if (countdown != null) {
                        println("Stopped ActivityModel")
                        countdown?.cancel()
                    }
                }
                "activityEntity.pause" -> {
                    if (countdown != null) {
                        println("Paused ActivityModel")
                        countdown?.pause()

                    }
                }
                "activityEntity.resume" -> {
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
