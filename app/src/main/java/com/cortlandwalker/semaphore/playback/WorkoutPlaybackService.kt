package com.cortlandwalker.semaphore.playback

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutPlaybackService : Service() {

    @Inject lateinit var controller: ForegroundWorkoutPlaybackController

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> startAsForeground(controller.currentNotification())
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAsForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                ForegroundWorkoutPlaybackController.ONGOING_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                ForegroundWorkoutPlaybackController.ONGOING_NOTIFICATION_ID,
                notification
            )
        }
    }

    companion object {
        const val ACTION_START = "com.cortlandwalker.semaphore.playback.START"
        const val ACTION_STOP = "com.cortlandwalker.semaphore.playback.STOP"
    }
}
