package xyz.cortland.semaphore.android.extensions

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.greenrobot.eventbus.EventBus
import xyz.cortland.semaphore.android.SemaphoreApp
import xyz.cortland.semaphore.android.R
import xyz.cortland.semaphore.android.database.AppDatabase
import xyz.cortland.semaphore.android.helpers.ACTIVITY_CHANNEL
import xyz.cortland.semaphore.android.helpers.ACTIVITY_FINISHED_ID
import xyz.cortland.semaphore.android.helpers.prefs
import xyz.cortland.semaphore.android.model.ActivityEntity
import xyz.cortland.semaphore.android.receivers.CountDownEvent
import xyz.cortland.semaphore.android.receivers.ActivityPlaybackReceiver
import java.util.concurrent.TimeUnit

fun Context.speakText(text: String) {
    SemaphoreApp.applicationContext().textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
}

fun Context.createTimerNotification(activityEntity: ActivityEntity, paused: Boolean): Notification {

    EventBus.getDefault().postSticky(CountDownEvent(activityEntity.countDownTimer!!))

    val intent = Intent(this, this::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

    val stopIntent = Intent(this, ActivityPlaybackReceiver::class.java)
    stopIntent.action = "activityEntity.stop"

    val pauseIntent = Intent(this, ActivityPlaybackReceiver::class.java)
    pauseIntent.action = "activityEntity.pause"

    val resumeIntent = Intent(this, ActivityPlaybackReceiver::class.java)
    resumeIntent.action = "activityEntity.resume"

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
        .setContentTitle(activityEntity.activityName)
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

fun Context.showTimerNotification(activityEntity: ActivityEntity, paused: Boolean) {
    val notification = createTimerNotification(activityEntity, paused)
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

fun Context.showAlert(title: String, message: String, OnClickListener: DialogInterface.OnClickListener, hasNegativeButton: Boolean = false) {

    val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    dialog.setTitle(title)
    dialog.setMessage(message)
    dialog.setPositiveButton("OK", OnClickListener)
    if (hasNegativeButton) {
        dialog.setNegativeButton("Cancel", null)
    }
    dialog.show()
}

val Context.semaphoreDB: AppDatabase? get() = AppDatabase.getAppDataBase(this)