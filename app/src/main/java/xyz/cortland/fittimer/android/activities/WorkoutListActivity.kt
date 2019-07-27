package xyz.cortland.fittimer.android.activities

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.adapter.WorkoutRecyclerViewAdapter
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.fragments.NewWorkoutDialogFragment

import xyz.cortland.fittimer.android.model.WorkoutModel


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

        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                mWorkouts.removeAt(position)
                item_list!!.adapter?.notifyItemRemoved(position)
                removeWorkoutItem(viewHolder.itemView.id)
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(item_list)

        fab.setOnClickListener {
            val newWorkoutFragment = NewWorkoutDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

    }
    private fun setupRecyclerView(recyclerView: RecyclerView) {

    }

    /**
     * Used to remove swiped Workouts
     * @param id: The id of the book
     */
    fun removeWorkoutItem(id: Int) {
        val dbHelper = WorkoutDatabase(this, null)
        val db = dbHelper.writableDatabase
        db.delete(WorkoutDatabase.TABLE_NAME, WorkoutDatabase.COLUMN_ID + "=" + id, null)
        db.close()
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
