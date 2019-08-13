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
import kotlin.concurrent.thread
import kotlin.reflect.KProperty


class WorkoutRecyclerViewAdapter(var parentActivity: WorkoutListActivity?, var mWorkouts: List<WorkoutModel>) : RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener
    var counterList: List<WorkoutModel> = ArrayList()
    var playingIndividual: Boolean = false
    var countdownTimer: CountDownTimer? = null
    var remainingTimeIndividual: Long? = null
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
            v.context.startActivity(intent)
            parentActivity!!.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
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
        this.counterList = mWorkouts
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = mWorkouts[position]

        val dbHandler = WorkoutDatabase(this.parentActivity!!, null)
        val cursor = dbHandler.getAllWorkouts()
        if (!cursor!!.moveToPosition(position)) {
            return
        }

        var seconds = mWorkouts[position].seconds
        seconds = seconds?.times(1000)

        if (mWorkouts.get(position).isDefaultState!!) {
            holder.stopButton.visibility = View.VISIBLE
            holder.workoutControls.visibility = View.GONE
            holder.resumeButton.visibility = View.GONE
            holder.pauseButton.visibility = View.VISIBLE
            holder.workoutImage.visibility = View.VISIBLE

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

        } else {
            return
        }

        holder.playButton.setOnClickListener {

            if (mWorkouts[position].workoutSpeech == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech?.speak(mWorkouts[position].workoutName, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    textToSpeech?.speak(mWorkouts[position].workoutName, TextToSpeech.QUEUE_FLUSH, null)
                }
            }

            holder.stopButton.visibility = View.VISIBLE
            holder.workoutControls.visibility = View.VISIBLE

            if (!parentActivity!!.playingAll) {

                if (!playingIndividual) {
                    playingIndividual = true
                } else {
                    playingIndividual = false
                }

                mWorkouts.get(position).isPlaying = true

                it.visibility = View.GONE

                countdownTimer = object: CountDownTimer(seconds.toLong(), 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        if (mWorkouts.get(position).isPlaying!!) {
                            holder.secondsView.text = "${millisUntilFinished / 1000}"
                            holder.workoutProgressBar.progress = (millisUntilFinished / 1000).toInt()
                        } else {
                            holder.secondsView.text = mWorkouts.get(position).seconds.toString()
                            cancel() // May have to run on different thread?
                        }
                    }

                    override fun onFinish() {
                        mWorkouts.get(position).isPlaying = false
                        it.visibility = View.VISIBLE
                        holder.secondsView.text = mWorkouts.get(position).seconds.toString()
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
            countdownTimer!!.cancel()
            holder.playButton.visibility = View.VISIBLE
            mWorkouts.get(position).isPlaying = false
            holder.secondsView.text = mWorkouts.get(position).seconds.toString()
            holder.workoutControls.visibility = View.GONE
            if (holder.resumeButton.visibility == View.VISIBLE) {
                holder.resumeButton.visibility = View.GONE
                holder.pauseButton.visibility = View.VISIBLE
            }
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
                countdownTimer!!.pause()
            }
        }

        holder.resumeButton.setOnClickListener {
            it.visibility = View.GONE
            holder.pauseButton.visibility = View.VISIBLE
            countdownTimer!!.resume()
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

        Log.d("Recyclerview", "Got to Play")

        Log.d("Recyclerview", "Seconds: ${workout.seconds.toString()}")

        Log.d("Recyclerview", "Position: $position")

        workout.isDefaultState = false

        Handler(Looper.getMainLooper()).post {

            if (mWorkouts[position].workoutSpeech == 1) {
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

            countdownTimer = object: CountDownTimer(seconds.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    Log.d("Recyclerview", "millisUntilFinished $millisUntilFinished")
                    mHolder.secondsView.text = "${millisUntilFinished / 1000}"
                    mHolder.workoutProgressBar.progress = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    if (position == mWorkouts.size - 1) {
                        Log.d("Recyclerview", "Finished")
                        Log.d("Recyclerview", "Workout Complete")
                        countdownTimer!!.cancel()
                        parentActivity!!.stopPlayingAll()
                        notifyDataSetChanged()
                    } else {
                        // Necessary because countdowntimer is weird and stops doing shit at 1
                        mHolder.secondsView.text = "0"
                        mHolder.workoutProgressBar.progress = 0

                        Log.d("Recyclerview", "Finished")
                        semaphore.release()
                    }
                }

            }
            countdownTimer!!.start()
        }
    }

    fun stopAllWorkouts() {
        countdownTimer!!.cancel()
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