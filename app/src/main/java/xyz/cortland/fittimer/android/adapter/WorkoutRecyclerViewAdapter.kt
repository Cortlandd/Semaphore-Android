package xyz.cortland.fittimer.android.adapter

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.workout_list_content.view.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.activities.WorkoutDetailActivity
import xyz.cortland.fittimer.android.activities.WorkoutListActivity
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.model.WorkoutModel
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore


class WorkoutRecyclerViewAdapter(var parentActivity: WorkoutListActivity?, var mWorkouts: List<WorkoutModel>) : RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener
    var textToSpeech: TextToSpeech? = null

    var workoutId: Int? = null

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as WorkoutModel
            workoutId = v.id
            val intent = Intent(v.context, WorkoutDetailActivity::class.java).apply {
                putExtra("arg_parcel_workout", item)
                putExtra("arg_workout_id", workoutId!!)
            }

            // TODO: Setup preferences to pause and later resume.
            if (item.countDownTimer?.hasStarted != null && item.countDownTimer?.hasStarted!!) {
                item.countDownTimer?.cancel()
                notifyDataSetChanged()
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(parentActivity!!, v, "viewWorkout")
            v.context.startActivity(intent, options.toBundle())
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        // TODO: Implement this elsewhere such as an application class to avoid leaks
        textToSpeech = TextToSpeech(parentActivity!!, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                // TODO: Create preferences for the apps country and set this. Also give users ability to change
                val language = textToSpeech?.setLanguage(Locale.US)
                if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language Not Supported.")
                } else {
                    println("Language Supported.")
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = mWorkouts[position]

        // TODO: Could be culprit of SQLITE memory leak. Verify
        // Before: val dbHandler = WorkoutDatabase(parentActivity!!, null)
        val dbHandler = parentActivity!!.dbHandler
        val cursor = dbHandler?.getAllWorkouts()
        if (!cursor!!.moveToPosition(position)) {
            return
        }

        var seconds = workout.seconds
        seconds = seconds?.times(1000)

        if (workout.isDefaultState!!) {
            holder.stopButton.visibility = View.VISIBLE
            holder.playButton.visibility = View.VISIBLE
            holder.workoutControls.visibility = View.GONE
            holder.resumeButton.visibility = View.GONE
            holder.pauseButton.visibility = View.VISIBLE
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
            holder.workoutProgressBar.max = seconds!! / 1000
            holder.workoutProgressBar.progress = seconds / 1000

            workout.countDownTimer?.cancel()

        } else {
            return
        }

        holder.playButton.setOnClickListener {

            if (workout.workoutSpeech == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech?.speak(workout.workoutName, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    textToSpeech?.speak(workout.workoutName, TextToSpeech.QUEUE_FLUSH, null)
                }
            }

            holder.stopButton.visibility = View.VISIBLE
            holder.workoutControls.visibility = View.VISIBLE

            if (!parentActivity!!.playingAll) {

                it.visibility = View.GONE

                workout.isDefaultState = false

                workout.countDownTimer = object: CountDownTimer(seconds.toLong(), 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        holder.secondsView.text = "${millisUntilFinished / 1000}"
                        holder.workoutProgressBar.progress = (millisUntilFinished / 1000).toInt()
                    }

                    override fun onFinish() {
                        it.visibility = View.VISIBLE
                        holder.secondsView.text = workout.seconds.toString()
                        holder.workoutControls.visibility = View.GONE

                        // Set the progress ring back to default
                        holder.workoutProgressBar.progress = seconds / 1000
                    }

                }.start()
            } else {
                Toast.makeText(
                    parentActivity,
                    "You are Playing All! To Play individual, please Stop Playing all",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        holder.stopButton.setOnClickListener {

            workout.countDownTimer!!.cancel()

            workout.isDefaultState = true

            notifyItemChanged(position)
        }

        holder.pauseButton.setOnClickListener {
            if (holder.secondsView.text == "0") {
                Toast.makeText(
                    parentActivity,
                    "Workout already complete.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                it.visibility = View.GONE
                holder.resumeButton.visibility = View.VISIBLE
                workout.countDownTimer!!.pause()
            }
        }

        holder.resumeButton.setOnClickListener {
            it.visibility = View.GONE
            holder.pauseButton.visibility = View.VISIBLE
            workout.countDownTimer!!.resume()
        }

        with(holder.itemView) {
            holder.itemView.id = cursor.getInt(cursor.getColumnIndex(WorkoutDatabase.COLUMN_ID))
            cursor.close()
            tag = workout
            setOnClickListener(onClickListener)
        }
    }

    fun play(mHolder: ViewHolder, position: Int, semaphore: Semaphore) {

        val workout = mWorkouts[position]

        var seconds = workout.seconds
        seconds = seconds?.times(1000)

        workout.isDefaultState = false

        Handler(Looper.getMainLooper()).post {

            if (workout.workoutSpeech == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech?.speak(workout.workoutName, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    textToSpeech?.speak(workout.workoutName, TextToSpeech.QUEUE_FLUSH, null)
                }
            }

            mHolder.workoutControls.visibility = View.VISIBLE
            mHolder.stopButton.visibility = View.GONE
            mHolder.playButton.visibility = View.GONE

            mHolder.workoutProgressBar.max = seconds!! / 1000

            workout.countDownTimer = object: CountDownTimer(seconds.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    mHolder.secondsView.text = "${millisUntilFinished / 1000}"
                    mHolder.workoutProgressBar.progress = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    if (position == mWorkouts.size - 1) {
                        parentActivity!!.stopPlayingAll()
                        notifyDataSetChanged()
                    } else {
                        // Necessary because countdowntimer is weird and stops doing shit at 1
                        mHolder.secondsView.text = "0"
                        mHolder.workoutProgressBar.progress = 0

                        semaphore.release()
                    }
                }

            }
            workout.countDownTimer!!.start()
        }
    }

    fun stopAllWorkouts() {
        mWorkouts.forEach {
            it.countDownTimer?.cancel()
        }
        notifyDataSetChanged()
    }

    // TODO: Figure out if i need this
    /*
    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }
    */

    override fun getItemCount() = mWorkouts.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var secondsView: TextView = view.seconds
        var workoutView: TextView = view.workout
        var playButton: Button = view.single_play_button
        var stopButton: Button = view.single_stop_button
        var pauseButton: Button = view.single_pause_button
        var resumeButton: Button = view.single_resume_button
        var workoutImage: ImageView = view.workout_image
        var workoutControls: LinearLayout = view.workout_controls
        var workoutProgressBar: ProgressBar = view.workout_progress_bar
    }

}