package com.cortlandwalker.semaphore.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.cortlandwalker.semaphore.R
import com.cortlandwalker.semaphore.core.MainActivity
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundWorkoutPlaybackController @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: WorkoutRepository
) : WorkoutPlaybackController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val notificationManager = NotificationManagerCompat.from(appContext)
    private val imageLoader = ImageLoader(appContext)
    private val _playbackState = MutableStateFlow(WorkoutPlaybackState())
    private var playbackJob: Job? = null
    private var currentBitmap: Bitmap? = null

    override val playbackState: StateFlow<WorkoutPlaybackState> = _playbackState.asStateFlow()

    override fun startSingle(workout: Workout) {
        startSession(workouts = listOf(workout), isPlayingAll = false)
    }

    override fun startAll(workouts: List<Workout>) {
        startSession(workouts = workouts, isPlayingAll = true)
    }

    override fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        currentBitmap = null
        _playbackState.value = WorkoutPlaybackState()
        stopForegroundService()
        notificationManager.cancel(ONGOING_NOTIFICATION_ID)
    }

    fun currentNotification(): Notification {
        ensureNotificationChannels()
        return buildOngoingNotification(_playbackState.value, currentBitmap)
    }

    private fun startSession(workouts: List<Workout>, isPlayingAll: Boolean) {
        if (workouts.isEmpty()) {
            stop()
            return
        }

        playbackJob?.cancel()
        ensureNotificationChannels()
        startForegroundService()

        playbackJob = scope.launch {
            var previousWorkoutName: String? = null

            workouts.forEachIndexed { index, workout ->
                val durationSeconds = workout.durationSeconds()
                var remainingSeconds = durationSeconds
                currentBitmap = loadNotificationBitmap(workout.displayImageUri)

                updatePlaybackState {
                    WorkoutPlaybackState(
                        isRunning = true,
                        isPlayingAll = isPlayingAll,
                        activeWorkoutId = workout.id,
                        activeWorkoutName = workout.name,
                        activeWorkoutImageUri = workout.displayImageUri,
                        activeWorkoutTimer = formatSecondsToHms(remainingSeconds),
                        remainingSeconds = remainingSeconds,
                        durationSeconds = durationSeconds,
                        playbackQueue = if (isPlayingAll) workouts.drop(index + 1).map { it.id } else emptyList()
                    )
                }

                if (previousWorkoutName != null) {
                    notifyTransition(
                        title = "$previousWorkoutName finished",
                        message = "Starting ${workout.name}"
                    )
                }

                while (remainingSeconds >= 0) {
                    updatePlaybackState {
                        _playbackState.value.copy(
                            activeWorkoutTimer = formatSecondsToHms(remainingSeconds),
                            remainingSeconds = remainingSeconds
                        )
                    }

                    if (remainingSeconds == 0) {
                        updateWorkoutAnalytics(workout, durationSeconds)
                        previousWorkoutName = workout.name
                        break
                    }

                    delay(1_000L)
                    remainingSeconds--
                }
            }

            notifyTransition(
                title = "Routine complete",
                message = if (isPlayingAll) "All workout timers finished." else "Workout timer finished."
            )
            stop()
        }
    }

    private fun updatePlaybackState(transform: () -> WorkoutPlaybackState) {
        _playbackState.value = transform()
        notificationManager.notify(
            ONGOING_NOTIFICATION_ID,
            buildOngoingNotification(_playbackState.value, currentBitmap)
        )
    }

    private suspend fun updateWorkoutAnalytics(workout: Workout, durationSeconds: Int) {
        val newStreak = if (wasPerformedYesterday(workout.lastPerformedAt)) workout.currentStreak + 1 else 1
        repo.update(
            workout.copy(
                completedCount = workout.completedCount + 1,
                totalTimeSpentSeconds = workout.totalTimeSpentSeconds + durationSeconds,
                lastPerformedAt = System.currentTimeMillis(),
                currentStreak = newStreak
            )
        )
    }

    private fun buildOngoingNotification(
        state: WorkoutPlaybackState,
        bitmap: Bitmap?
    ): Notification {
        val openAppIntent = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            appContext,
            1,
            Intent(appContext, WorkoutPlaybackService::class.java).apply {
                action = WorkoutPlaybackService.ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = state.activeWorkoutName ?: appContext.getString(R.string.app_name)
        val message = when {
            state.activeWorkoutTimer != null -> "${state.activeWorkoutTimer} remaining"
            else -> "Preparing workout timer"
        }

        return NotificationCompat.Builder(appContext, ONGOING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(if (state.isPlayingAll) "Routine in progress" else "Timer in progress")
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                0,
                "Stop",
                stopIntent
            )
            .apply {
                if (bitmap != null) {
                    setLargeIcon(bitmap)
                    setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null as Bitmap?)
                            .setBigContentTitle(title)
                            .setSummaryText(message)
                    )
                }
            }
            .build()
    }

    private fun notifyTransition(title: String, message: String) {
        notificationManager.notify(
            TRANSITION_NOTIFICATION_ID,
            NotificationCompat.Builder(appContext, ALERTS_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTimeoutAfter(6_000L)
                .build()
        )
    }

    private suspend fun loadNotificationBitmap(imageUri: String?): Bitmap? {
        if (imageUri.isNullOrBlank()) return null

        return withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(appContext)
                    .data(imageUri)
                    .allowHardware(false)
                    .build()

                val result = imageLoader.execute(request)
                (result as? SuccessResult)?.drawable?.toBitmap()
            }.getOrNull()
        }
    }

    private fun ensureNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = appContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                ONGOING_CHANNEL_ID,
                "Workout timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active Semaphore workout timers."
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                ALERTS_CHANNEL_ID,
                "Workout alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a timer finishes or the next timer begins."
            }
        )
    }

    private fun startForegroundService() {
        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, WorkoutPlaybackService::class.java).apply {
                action = WorkoutPlaybackService.ACTION_START
            }
        )
    }

    private fun stopForegroundService() {
        appContext.startService(
            Intent(appContext, WorkoutPlaybackService::class.java).apply {
                action = WorkoutPlaybackService.ACTION_STOP
            }
        )
    }

    private fun wasPerformedYesterday(lastPerformedAt: Long?): Boolean {
        if (lastPerformedAt == null) return false

        val lastDate = Calendar.getInstance().apply { timeInMillis = lastPerformedAt }
        val today = Calendar.getInstance()

        lastDate.set(Calendar.HOUR_OF_DAY, 0)
        lastDate.set(Calendar.MINUTE, 0)
        lastDate.set(Calendar.SECOND, 0)
        lastDate.set(Calendar.MILLISECOND, 0)

        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        today.add(Calendar.DAY_OF_YEAR, -1)

        return lastDate.timeInMillis == today.timeInMillis
    }

    private fun Workout.durationSeconds(): Int {
        return (hours * 3600) + (minutes * 60) + seconds
    }

    private fun formatSecondsToHms(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) {
            "%02d:%02d:%02d".format(h, m, s)
        } else {
            "%02d:%02d".format(m, s)
        }
    }

    companion object {
        const val ONGOING_NOTIFICATION_ID = 3001
        const val TRANSITION_NOTIFICATION_ID = 3002
        private const val ONGOING_CHANNEL_ID = "workout_playback_ongoing"
        private const val ALERTS_CHANNEL_ID = "workout_playback_alerts"
    }
}
