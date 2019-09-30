package xyz.cortland.fittimer.android.extensions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.greenrobot.eventbus.EventBus
import xyz.cortland.fittimer.android.SemaphoreApp
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.database.ActivityDatabase
import xyz.cortland.fittimer.android.helpers.ACTIVITY_CHANNEL
import xyz.cortland.fittimer.android.helpers.ACTIVITY_FINISHED_ID
import xyz.cortland.fittimer.android.helpers.prefs
import xyz.cortland.fittimer.android.model.ActivityModel
import xyz.cortland.fittimer.android.receivers.CountDownEvent
import xyz.cortland.fittimer.android.receivers.ActivityPlaybackReceiver
import java.util.concurrent.TimeUnit

fun Context.speakText(text: String) {
    SemaphoreApp.applicationContext().textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
}

val Context.dbHandler: ActivityDatabase get() = ActivityDatabase(applicationContext, null)

fun Context.createTimerNotification(activityModel: ActivityModel, paused: Boolean): Notification {

    EventBus.getDefault().postSticky(CountDownEvent(activityModel.countDownTimer!!))

    val intent = Intent(this, this::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

    val stopIntent = Intent(this, ActivityPlaybackReceiver::class.java)
    stopIntent.action = "activityModel.stop"

    val pauseIntent = Intent(this, ActivityPlaybackReceiver::class.java)
    pauseIntent.action = "activityModel.pause"

    val resumeIntent = Intent(this, ActivityPlaybackReceiver::class.java)
    resumeIntent.action = "activityModel.resume"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannel(ACTIVITY_CHANNEL, "activity_channel", NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableVibration(false)
            setSound(null, null)
            notificationManager.createNotificationChannel(this)
        }
    }

    val _hours = TimeUnit.MILLISECONDS.toHours(prefs.currentPlayingAllRemainingTime)
    val _minutes = TimeUnit.MILLISECONDS.toMinutes(prefs.currentPlayingAllRemainingTime) % TimeUnit.HOURS.toMinutes(1)
    val _seconds = TimeUnit.MILLISECONDS.toSeconds(prefs.currentPlayingAllRemainingTime) % TimeUnit.MINUTES.toSeconds(1)

    var numMessages = 0

    // TODO: Figure out why contentIntent doesn't just open activity after a while
    val notificationBuider = NotificationCompat.Builder(this, ACTIVITY_CHANNEL)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(activityModel.activityName)
        .setContentText("$_hours hr $_minutes min $_seconds sec remaining.")
        .setNumber(++numMessages)
        .setSound(null)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setShowWhen(false)
        .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
        .addAction(R.drawable.ic_stop_24dp, "Stop", PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT))

    if (paused) {
        notificationBuider.addAction(android.R.drawable.ic_media_play, "Resume", PendingIntent.getBroadcast(this, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT))
    } else {
        notificationBuider.addAction(android.R.drawable.ic_media_pause, "Pause", PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    val notification = notificationBuider.build()

    return notification

}

fun Context.showTimerNotification(activityModel: ActivityModel, paused: Boolean) {
    val notification = createTimerNotification(activityModel, paused)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(ACTIVITY_FINISHED_ID, notification)
}

fun Context.hideTimerNotificationHelper(id: Int) {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(id)
}

fun Context.hideTimerNotification(){
    hideTimerNotificationHelper(ACTIVITY_FINISHED_ID)
}
