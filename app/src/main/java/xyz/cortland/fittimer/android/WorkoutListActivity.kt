package xyz.cortland.fittimer.android

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list_content.view.*
import kotlinx.android.synthetic.main.workout_list.*
import xyz.cortland.fittimer.android.adapter.WorkoutRecyclerViewAdapter
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.fragments.NewWorkoutDialogFragment
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.model.WorkoutModel
import java.lang.Exception


/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [WorkoutDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class WorkoutListActivity : AppCompatActivity(), NewWorkoutDialogFragment.NewWorkoutDialogListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    var workoutAdapter: WorkoutRecyclerViewAdapter? = null

    val dbHandler = WorkoutDatabase(this, null)

    val mWorkouts: ArrayList<WorkoutModel> = ArrayList<WorkoutModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        if (item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        val dbHandler = WorkoutDatabase(this, null)
        mWorkouts.addAll(dbHandler.allWorkoutsList())
        workoutAdapter = WorkoutRecyclerViewAdapter(this, mWorkouts, false)
        item_list.adapter = workoutAdapter

        fab.setOnClickListener {
            val newWorkoutFragment = NewWorkoutDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

    }
    private fun setupRecyclerView(recyclerView: RecyclerView) {

    }

    override fun onSaveClick(dialog: DialogFragment, workout: WorkoutModel) {
        dbHandler.addWorkout(workout)
        mWorkouts.clear()
        mWorkouts.addAll(dbHandler.allWorkoutsList())
        workoutAdapter?.notifyDataSetChanged()
        dialog.dismiss()
    }

    override fun onCancelClick(dialog: DialogFragment) {
        dialog.dismiss()
    }


}
