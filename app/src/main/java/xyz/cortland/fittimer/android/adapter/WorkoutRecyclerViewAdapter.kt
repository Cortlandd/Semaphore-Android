package xyz.cortland.fittimer.android.adapter

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.workout_list_content.view.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.activities.WorkoutDetailActivity
import xyz.cortland.fittimer.android.activities.WorkoutListActivity
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.model.WorkoutModel

class WorkoutRecyclerViewAdapter() : RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener
    var workoutList: List<WorkoutModel> = ArrayList()
    var counterList: List<WorkoutModel> = ArrayList()
    var playingIndividual: Boolean = false
    var countdownTimer: CountDownTimer? = null
    var remainingTimeIndividual: Long? = null
    var mWorkouts: List<WorkoutModel>? = null
    var parentActivity: WorkoutListActivity? = null

    constructor(parentActivity: WorkoutListActivity?, mWorkouts: List<WorkoutModel>): this() {
        this.parentActivity = parentActivity
        this.mWorkouts = mWorkouts
    }

    constructor(workoutList: List<WorkoutModel>, counterList: List<WorkoutModel>) : this() {
        this.workoutList = workoutList
        this.counterList = counterList
    }

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as WorkoutModel
            val intent = Intent(v.context, WorkoutDetailActivity::class.java).apply {
                putExtra("arg_parcel_workout", item)
            }
            v.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = mWorkouts?.get(position)
        holder.workoutView.text = workout?.workoutName
        holder.secondsView.text = workout?.seconds.toString()

        val dbHandler = WorkoutDatabase(this.parentActivity!!, null)
        val cursor = dbHandler.getAllWorkouts()
        if (!cursor!!.moveToPosition(position)) {
            return
        }
        holder.itemView.id = cursor.getInt(cursor.getColumnIndex(WorkoutDatabase.COLUMN_ID))
        cursor.close()

        with(holder.itemView) {
            tag = workout
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = mWorkouts!!.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var secondsView: TextView = view.seconds
        var workoutView: TextView = view.workout
    }

}