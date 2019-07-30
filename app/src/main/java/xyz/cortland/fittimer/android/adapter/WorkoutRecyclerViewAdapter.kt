package xyz.cortland.fittimer.android.adapter

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list_content.view.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.activities.WorkoutDetailActivity
import xyz.cortland.fittimer.android.activities.WorkoutListActivity
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.model.WorkoutModel
import kotlin.concurrent.thread

class WorkoutRecyclerViewAdapter(var parentActivity: WorkoutListActivity?, var mWorkouts: List<WorkoutModel>) : RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>() {

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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_list_content, parent, false)
        this.counterList = mWorkouts
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = mWorkouts.get(position)
        holder.workoutView.text = workout.workoutName
        holder.secondsView.text = workout.seconds.toString()

        val dbHandler = WorkoutDatabase(this.parentActivity!!, null)
        val cursor = dbHandler.getAllWorkouts()
        if (!cursor!!.moveToPosition(position)) {
            return
        }

        var seconds = mWorkouts[position].seconds
        seconds = seconds?.times(1000)

        if (counterList[position].isCount!!) {
            parentActivity!!.countdownPlayAll = object: CountDownTimer(seconds!!.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    if (parentActivity!!.playingAll) {
                        holder.secondsView.text = "${millisUntilFinished / 1000}"
                    }
                }

                override fun onFinish() {

                    counterList.get(position).isCount = false

                    if (position == counterList.size - 1) {
                        parentActivity!!.playingAll = false
                        parentActivity!!.stopAllButton?.visibility = View.GONE
                        parentActivity!!.playAllButton?.visibility = View.VISIBLE
                    } else {
                        if (parentActivity!!.playingAll) {
                            counterList.get(position + 1).isCount = true
                            notifyDataSetChanged()
                        }
                    }
                }

            }
            thread {
                parentActivity!!.countdownPlayAll?.start()
            }.run()
        }

        holder.playButton.setOnClickListener {

            if (!parentActivity!!.playingAll) {

                if (!playingIndividual) {
                    playingIndividual = true
                } else {
                    playingIndividual = false
                }

                mWorkouts.get(position).isPlaying = true

                it.visibility = View.GONE
                holder.stopButton.visibility = View.VISIBLE

                countdownTimer = object: CountDownTimer(seconds!!.toLong(), 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        if (mWorkouts.get(position).isPlaying!!) {
                            holder.secondsView.text = "${millisUntilFinished / 1000}"
                            mWorkouts.get(position).remainingSeconds = millisUntilFinished

                        } else {
                            holder.secondsView.text = mWorkouts.get(position).seconds.toString()
                            cancel() // May have to run on different thread?
                        }
                    }

                    override fun onFinish() {
                        mWorkouts.get(position).isPlaying = false
                        it.visibility = View.VISIBLE
                        holder.stopButton.visibility = View.GONE
                        holder.secondsView.text = mWorkouts.get(position).seconds.toString()
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
            it.visibility = View.GONE
            holder.playButton.visibility = View.VISIBLE
            mWorkouts.get(position).isPlaying = false
            holder.secondsView.text = mWorkouts.get(position).seconds.toString()
        }

        with(holder.itemView) {
            holder.itemView.id = cursor.getInt(cursor.getColumnIndex(WorkoutDatabase.COLUMN_ID))
            cursor.close()
            tag = workout
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = mWorkouts!!.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var secondsView: TextView = view.seconds
        var workoutView: TextView = view.workout
        var playButton: Button = view.single_play_button
        var stopButton: Button = view.single_stop_button
    }

}