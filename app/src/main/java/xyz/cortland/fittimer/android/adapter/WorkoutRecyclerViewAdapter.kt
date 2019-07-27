package xyz.cortland.fittimer.android.adapter

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.workout_list_content.view.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.WorkoutDetailActivity
import xyz.cortland.fittimer.android.WorkoutDetailFragment
import xyz.cortland.fittimer.android.WorkoutListActivity
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.model.WorkoutModel

class WorkoutRecyclerViewAdapter(private val parentActivity: WorkoutListActivity, private val mWorkouts: List<WorkoutModel>, private val twoPane: Boolean) : RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener
    var counterList: List<WorkoutModel> = ArrayList()
    var playingIndividual: Boolean = false
    var countdownTimer: CountDownTimer? = null
    var remainingTimeIndividual: Long? = null

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as Workout
            if (twoPane) {
                val fragment = WorkoutDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(WorkoutDetailFragment.ARG_ITEM_ID, item.id)
                    }
                }
                parentActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(v.context, WorkoutDetailActivity::class.java).apply {
                    putExtra(WorkoutDetailFragment.ARG_ITEM_ID, item.id)
                }
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = mWorkouts[position]
        holder.workoutView.text = workout.workoutName
        holder.secondsView.text = workout.seconds.toString()

        holder.playButton.setOnClickListener {

            val s = holder.secondsView.text.toString()
            val secondsRemaining = s.toInt() * 1000
            println("Seconds Remaining: ${secondsRemaining.toLong()}")
            val countdownTimer = object: CountDownTimer(secondsRemaining.toLong(), 1000) {

                override fun onFinish() {
                    holder.secondsView.text = s
                }

                override fun onTick(millisUntilFinished: Long) {
                    var seconds = millisUntilFinished / 1000
                    holder.secondsView.text = seconds.toString()
                }
            }
            countdownTimer.start()

        }

        with(holder.itemView) {
            tag = workout
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = mWorkouts.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var idView: TextView = view.id_text
        var secondsView: TextView = view.seconds
        var workoutView: TextView = view.workout
        var playButton: Button = view.single_play_button
    }

    fun playAll() {
        println("Play button")
    }
}