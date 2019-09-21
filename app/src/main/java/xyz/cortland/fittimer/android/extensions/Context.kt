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
import xyz.cortland.fittimer.android.FitTimer
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.helpers.WORKOUT_CHANNEL
import xyz.cortland.fittimer.android.helpers.WORKOUT_FINISHED_ID
import xyz.cortland.fittimer.android.helpers.prefs
import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.receivers.CountDownEvent
import xyz.cortland.fittimer.android.receivers.WorkoutPlaybackReceiver
import java.util.concurrent.TimeUnit

fun Context.speakText(text: String) {
    FitTimer.applicationContext().textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
}

val Context.dbHandler: WorkoutDatabase get() = WorkoutDatabase(applicationContext, null)

fun Context.createTimerNotification(workout: Workout, paused: Boolean): Notification {

    EventBus.getDefault().postSticky(CountDownEvent(workout.countDownTimer!!))

    val intent = Intent(this, this::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

    val stopIntent = Intent(this, WorkoutPlaybackReceiver::class.java)
    stopIntent.action = "workout.stop"

    val pauseIntent = Intent(this, WorkoutPlaybackReceiver::class.java)
    pauseIntent.action = "workout.pause"

    val resumeIntent = Intent(this, WorkoutPlaybackReceiver::class.java)
    resumeIntent.action = "workout.resume"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannel(WORKOUT_CHANNEL, "workout_channel", NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableVibration(true)
            setSound(null, null)
            notificationManager.createNotificationChannel(this)
        }
    }

    val _hours = TimeUnit.MILLISECONDS.toHours(prefs.currentPlayingAllRemainingTime)
    val _minutes = TimeUnit.MILLISECONDS.toMinutes(prefs.currentPlayingAllRemainingTime) % TimeUnit.HOURS.toMinutes(1)
    val _seconds = TimeUnit.MILLISECONDS.toSeconds(prefs.currentPlayingAllRemainingTime) % TimeUnit.MINUTES.toSeconds(1)

    var numMessages = 0

    // TODO: Figure out why contentIntent doesn't just open activity after a while
    val notificationBuider = NotificationCompat.Builder(this, WORKOUT_CHANNEL)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(workout.workoutName)
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

fun Context.showTimerNotification(workout: Workout, paused: Boolean) {
    val notification = createTimerNotification(workout, paused)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(WORKOUT_FINISHED_ID, notification)
}

fun Context.hideTimerNotificationHelper(id: Int) {
    val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.cancel(id)
}

fun Context.hideTimerNotification(){
    hideTimerNotificationHelper(WORKOUT_FINISHED_ID)
}
