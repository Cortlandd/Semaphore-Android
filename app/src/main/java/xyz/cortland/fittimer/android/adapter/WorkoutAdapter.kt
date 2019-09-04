package xyz.cortland.fittimer.android.adapter

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.tapadoo.alerter.Alerter
import io.karn.notify.Notify
import kotlinx.android.synthetic.main.workout_list_content.view.*
import org.greenrobot.eventbus.EventBus
import xyz.cortland.fittimer.android.FitTimer
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.activities.WorkoutDetailActivity
import xyz.cortland.fittimer.android.activities.WorkoutListActivity
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.extensions.dbHandler
import xyz.cortland.fittimer.android.extensions.hideTimerNotification
import xyz.cortland.fittimer.android.extensions.showTimerNotification
import xyz.cortland.fittimer.android.extensions.speakText
import xyz.cortland.fittimer.android.fragments.NewWorkoutDialogFragment
import xyz.cortland.fittimer.android.helpers.CURRENT_PLAYING_ALL_WORKOUT_POSITION
import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.receivers.WorkoutFinishedReceiver
import xyz.cortland.fittimer.android.helpers.CURRENT_PLAYING_ALL_WORKOUT_REMAINING
import xyz.cortland.fittimer.android.helpers.WORKOUT_FINISHED_ID
import xyz.cortland.fittimer.android.helpers.prefs
import xyz.cortland.fittimer.android.receivers.CountDownEvent
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore


class WorkoutAdapter(var parentActivity: WorkoutListActivity?, var mWorkouts: List<Workout>) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener
    private val onLongClickListener: View.OnLongClickListener

    var workoutId: Int? = null

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as Workout
            workoutId = v.id
            val intent = Intent(v.context, WorkoutDetailActivity::class.java).apply {
                putExtra("arg_parcel_workout", item)
                putExtra("arg_workout_id", workoutId!!)
            }

            // TODO: Setup preferences to pause and later resume.
            if (item.countDownTimer?.hasStarted != null && item.countDownTimer?.hasStarted!!) {
                item.countDownTimer?.cancel()
                item.isDefaultState = true
                notifyDataSetChanged()
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(parentActivity!!, v, "viewWorkout")
            v.context.startActivity(intent, options.toBundle())
        }

        // TODO: Resolve issue where a workout is playing and edited
        onLongClickListener = View.OnLongClickListener { v ->
            val workout = v.tag as Workout
            workoutId = v.id
            prefs!!.longPressWorkoutId = v.id
            prefs.editingLongPressWorkout = true
            val newWorkoutDialogFragment: NewWorkoutDialogFragment = NewWorkoutDialogFragment.newInstance(workout, workoutId!!)
            newWorkoutDialogFragment.show(parentActivity!!.supportFragmentManager, "ModifyWorkout")

            true

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = mWorkouts[position]

        val cursor = FitTimer.applicationContext().dbHandler.getAllWorkouts()
        if (!cursor!!.moveToPosition(position)) {
            return
        }

        var seconds = workout.seconds
        seconds = seconds?.times(1000)

        if (workout.isDefaultState!!) {
            holder.stopButton.show()
            holder.playButton.show()
            holder.workoutControls.visibility = View.GONE
            holder.resumeButton.hide()
            holder.pauseButton.show()
            holder.workoutImage.visibility = View.VISIBLE
            holder.itemView.isEnabled = true

            holder.workoutView.text = workout.workoutName
            holder.secondsView.text = workout.seconds.toString()
            if (workout.workoutImage != null) {
                Glide.with(parentActivity!!).load(File(workout.workoutImage)).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.workoutImage)
            } else {
                holder.workoutImage.visibility = View.GONE
            }

            // Mostly for Play All
            holder.workoutProgressBar.progressMax = (seconds!! / 1000).toFloat()
            holder.workoutProgressBar.progress = (seconds / 1000).toFloat()

            workout.isPlayingAll = false

        } else {
            return
        }

        workout.countDownTimer = object: CountDownTimer(seconds.toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                holder.secondsView.text = "${millisUntilFinished / 1000}"
                holder.workoutProgressBar.setProgressWithAnimation((millisUntilFinished / 1000).toFloat())
            }

            override fun onFinish() {
                if (parentActivity!!.isPaused!! == true) {
                    finishedWorkoutNotification(workout)
                }
                holder.playButton.show()
                holder.secondsView.text = workout.seconds.toString()
                holder.workoutControls.visibility = View.GONE

                // Set the progress ring back to default
                holder.workoutProgressBar.progress = (seconds / 1000).toFloat()
            }

            override fun countdownStart() {

                if (workout.workoutSpeech == 1) {
                    // TODO: Need shared textToSpeech
                    parentActivity!!.speakText(workout.workoutName!!)
                }

                holder.playButton.hide()

                holder.workoutControls.visibility = View.VISIBLE

            }

            override fun countdownCancel() {

                workout.isDefaultState = true

                notifyItemChanged(position)
            }

            override fun countdownPause() {
                holder.pauseButton.hide()
                holder.resumeButton.show()
            }

            override fun countdownResume() {
                holder.resumeButton.hide()
                holder.pauseButton.show()
            }

        }

        holder.playButton.setOnClickListener {
            workout.countDownTimer!!.start()
        }

        // TODO: Implement full logic for Stop button on Playing All
        holder.stopButton.setOnClickListener {
            workout.countDownTimer!!.cancel()
        }

        holder.pauseButton.setOnClickListener {
            workout.countDownTimer!!.pause()
        }

        holder.resumeButton.setOnClickListener {
            workout.countDownTimer!!.resume()
        }

        with(holder.itemView) {
            holder.itemView.id = cursor.getInt(cursor.getColumnIndex(WorkoutDatabase.COLUMN_ID))
            cursor.close()
            tag = workout
            setOnClickListener(onClickListener)
            setOnLongClickListener(onLongClickListener)
        }
    }

    fun play(mHolder: ViewHolder, position: Int, semaphore: Semaphore) {

        val workout = mWorkouts[position]

        workout.isPlayingAll = true

        var seconds = workout.seconds
        seconds = seconds?.times(1000)

        workout.isDefaultState = false

        Handler(Looper.getMainLooper()).post {

            //mHolder.stopButton.hide()
            //mHolder.playButton.hide()

            mHolder.workoutProgressBar.progressMax = (seconds!! / 1000).toFloat()

            workout.countDownTimer = object: CountDownTimer(seconds.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    mHolder.secondsView.text = "${millisUntilFinished / 1000}"
                    workout.remainingSeconds = (millisUntilFinished / 1000).toInt()
                    prefs!!.currentPlayingAllRemainingTime = workout.remainingSeconds!!
                    if (parentActivity!!.isPaused!!) {
                        //updateNotification(workout,"${millisUntilFinished / 1000} seconds remaining.")
                        parentActivity!!.showTimerNotification(workout, false)
                    }
                    mHolder.workoutProgressBar.setProgressWithAnimation((millisUntilFinished / 1000).toFloat())
                }

                override fun onFinish() {
                    if (position == mWorkouts.size - 1) {
                        workout.remainingSeconds = 0
                        prefs!!.removePreferences(CURRENT_PLAYING_ALL_WORKOUT_REMAINING)
                        prefs.removePreferences(CURRENT_PLAYING_ALL_WORKOUT_POSITION)
                        parentActivity!!.playingAll = false
                        parentActivity!!.validatePlayAll()
                        if (parentActivity!!.isPaused!!) {
                            parentActivity!!.hideTimerNotification()
                            finishedWorkout()
                        } else {
                            Alerter.create(parentActivity)
                                .setTitle("Workout Complete!")
                                .setDuration(500)
                                .setBackgroundColorRes(R.color.colorAccent)
                                .enableSwipeToDismiss()
                                .enableVibration(true)
                                .show()
                        }
                    } else {
                        // Necessary because countdowntimer is weird and stops doing shit at 1
                        if (parentActivity!!.isPaused!!) {
                            parentActivity!!.hideTimerNotification()
                        }
                        //workout.isPlayingAll = false
                        mHolder.secondsView.text = "0"
                        workout.remainingSeconds = 0
                        prefs!!.removePreferences(CURRENT_PLAYING_ALL_WORKOUT_REMAINING)
                        prefs.removePreferences(CURRENT_PLAYING_ALL_WORKOUT_POSITION)
                        //mHolder.workoutProgressBar.progress = 0
                        semaphore.release()
                    }
                }

                override fun countdownStart() {

                    prefs!!.currentPlayingAllWorkoutPosition = position

                    if (workout.workoutSpeech == 1) {
                        // TODO: Need shared textToSpeech
                        parentActivity!!.speakText(workout.workoutName!!)
                    }

                    mHolder.workoutControls.visibility = View.VISIBLE

                    mHolder.playButton.hide()

                }

                override fun countdownCancel() {

                    when {
                        position != mWorkouts.size - 1 -> {
                            if (parentActivity!!.isPaused!!) {
                                parentActivity!!.hideTimerNotification()
                            }
                            mHolder.secondsView.text = "0"
                            workout.remainingSeconds = 0
                            mHolder.workoutProgressBar.progress = 0f
                            prefs!!.removePreferences(CURRENT_PLAYING_ALL_WORKOUT_REMAINING)
                            prefs.removePreferences(CURRENT_PLAYING_ALL_WORKOUT_POSITION)
                            //mHolder.workoutProgressBar.progress = 0
                            if (parentActivity!!.playingAll) {
                                semaphore.release()
                            } else {
                                return
                            }
                        }
                        position == mWorkouts.size - 1 -> {
                            workout.remainingSeconds = 0
                            prefs!!.removePreferences(CURRENT_PLAYING_ALL_WORKOUT_REMAINING)
                            prefs.removePreferences(CURRENT_PLAYING_ALL_WORKOUT_POSITION)
                            parentActivity!!.playingAll = false
                            parentActivity!!.validatePlayAll()
                            if (parentActivity!!.isPaused!!) {
                                parentActivity!!.hideTimerNotification()
                                finishedWorkout()
                            } else {
                                Alerter.create(parentActivity)
                                    .setTitle("Workout Complete!")
                                    .setDuration(500)
                                    .enableSwipeToDismiss()
                                    .enableVibration(true)
                                    .setBackgroundColorRes(R.color.colorAccent)
                                    .show()
                            }
                        }
                        else -> {
                            return
                        }
                    }
                }

                override fun countdownPause() {
                    parentActivity!!.showTimerNotification(workout, true)
                    mHolder.pauseButton.hide()
                    mHolder.resumeButton.show()
                }

                override fun countdownResume() {
                    parentActivity!!.showTimerNotification(workout, false)
                    mHolder.resumeButton.hide()
                    mHolder.pauseButton.show()
                }

            }
            workout.countDownTimer!!.start()
        }
    }

    fun finishedWorkoutNotification(workout: Workout) {

        val resultIntent = Intent(parentActivity!!.applicationContext, parentActivity!!::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        Notify
            .with(parentActivity!!)
            .content {
                title = "Finished"
                text = "${workout.workoutName}\nfor ${workout.seconds.toString()} seconds."
            }
            .meta {
                clickIntent = PendingIntent.getActivity(parentActivity!!.applicationContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            .header {
                color = 0x706DF8 // Set the color of the bell icon
            }
            .show()
    }

    fun stopAllWorkouts() {
        mWorkouts.forEach {
            it.countDownTimer?.cancel()
            it.isDefaultState = true
        }
        notifyDataSetChanged()
    }

    fun finishedWorkout() {
        val intent = Intent(parentActivity!!, WorkoutFinishedReceiver::class.java)
        intent.action = "playingall.workout.finished"
        parentActivity!!.sendBroadcast(intent)
    }

    override fun getItemCount() = mWorkouts.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var secondsView: TextView = view.seconds
        var workoutView: TextView = view.workout
        var playButton: FloatingActionButton = view.single_play_button
        var stopButton: FloatingActionButton = view.single_stop_button
        var pauseButton: FloatingActionButton = view.single_pause_button
        var resumeButton: FloatingActionButton = view.single_resume_button
        var workoutImage: ImageView = view.workout_image
        var workoutControls: LinearLayout = view.workout_controls
        var workoutProgressBar: CircularProgressBar = view.workout_progress_bar
    }

}