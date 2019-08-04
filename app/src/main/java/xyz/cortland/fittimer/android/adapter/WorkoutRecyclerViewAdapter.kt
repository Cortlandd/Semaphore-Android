package xyz.cortland.fittimer.android.adapter

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list_content.view.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.activities.WorkoutDetailActivity
import xyz.cortland.fittimer.android.activities.WorkoutListActivity
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.model.WorkoutModel
import xyz.cortland.fittimer.android.utils.SlideAnimationUtil
import java.io.File
import kotlin.concurrent.thread



class WorkoutRecyclerViewAdapter(var parentActivity: WorkoutListActivity?, var mWorkouts: List<WorkoutModel>) : androidx.recyclerview.widget.RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener
    var counterList: List<WorkoutModel> = ArrayList()
    var playingIndividual: Boolean = false
    var countdownTimer: CountDownTimer? = null
    var remainingTimeIndividual: Long? = null

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
                Glide.with(parentActivity!!).load(File(workout.workoutImage)).into(holder.workoutImage)
            } else {
                holder.workoutImage.visibility = View.GONE
            }

            // Mostly for Play All
            holder.workoutProgressBar.max = seconds!! / 1000
            holder.workoutProgressBar.progress = seconds / 1000

        } else {
            return
        }

        // TODO: Show controls for playing all
        if (counterList[position].isCount!!) {

            holder.workoutControls.visibility = View.VISIBLE
            holder.stopButton.visibility = View.GONE

            holder.workoutProgressBar.max = seconds / 1000

            parentActivity!!.countdownPlayAll = object: CountDownTimer(seconds.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    if (parentActivity!!.playingAll) {
                        holder.secondsView.text = "${millisUntilFinished / 1000}"
                        holder.workoutProgressBar.progress = (millisUntilFinished / 1000).toInt()
                    }
                }

                override fun onFinish() {
                    counterList.get(position).isCount = false

                    if (position == counterList.size - 1) {
                        // Happens when Play All is finished
                        parentActivity!!.playingAll = false
                        parentActivity!!.stopAllButton?.visibility = View.GONE
                        parentActivity!!.playAllButton?.visibility = View.VISIBLE
                        notifyDataSetChanged()
                    } else {
                        if (parentActivity!!.playingAll) {
                            counterList.get(position + 1).isCount = true
                            // Might be used for animation
                            //notifyItemChanged(position)
                            notifyDataSetChanged()
                        }
                    }
                }
            }
            parentActivity!!.countdownPlayAll?.start()
        }

        holder.playButton.setOnClickListener {

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
                            mWorkouts.get(position).remainingSeconds = millisUntilFinished
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

                }
                thread {
                    countdownTimer!!.start()
                }.run()
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
            it.visibility = View.GONE
            holder.resumeButton.visibility = View.VISIBLE
            countdownTimer!!.pause()
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