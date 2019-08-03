package xyz.cortland.fittimer.android.activities

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list.*
import kotlinx.android.synthetic.main.workout_list_content.view.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.adapter.WorkoutRecyclerViewAdapter
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.fragments.NewWorkoutDialogFragment

import xyz.cortland.fittimer.android.model.WorkoutModel
import java.io.File


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
    //private var twoPane: Boolean = false

    var workoutAdapter: WorkoutRecyclerViewAdapter? = null

    var dbHandler = WorkoutDatabase(this, null)

    val mWorkouts: ArrayList<WorkoutModel> = ArrayList<WorkoutModel>()

    var countdownPlayAll: CountDownTimer? = null

    var playingAll: Boolean = false

    var playAllButton: Button? = null

    var stopAllButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

//        if (item_detail_container != null) {
//            // The detail container view will be present only in the
//            // large-screen layouts (res/values-w900dp).
//            // If this view is present, then the
//            // activity should be in two-pane mode.
//            twoPane = true
//        }

        mWorkouts.addAll(dbHandler.allWorkoutsList())
        workoutAdapter = WorkoutRecyclerViewAdapter(this, mWorkouts)
        item_list.adapter = workoutAdapter

        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                var image = mWorkouts.get(position).workoutImage
                mWorkouts.removeAt(position)
                item_list!!.adapter?.notifyItemRemoved(position)
                removeWorkoutItem(viewHolder.itemView.id)
                if (image != null) {
                    var df = File(image)
                    df.delete()
                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(item_list)

        setupView()

        fab.setOnClickListener {
            val newWorkoutFragment = NewWorkoutDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

    }

    override fun onResume() {
        super.onResume()
        mWorkouts.clear()
        mWorkouts.addAll(dbHandler.allWorkoutsList())
        workoutAdapter!!.notifyDataSetChanged()
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

    fun setupView() {

        playAllButton = findViewById<Button>(R.id.play_all_button)
        stopAllButton = findViewById<Button>(R.id.stop_all_button)

        playAllButton!!.setOnClickListener {
            validatePlayAll()
        }
        stopAllButton!!.setOnClickListener {
            stopPlayingAll()
        }

    }

    private fun validatePlayAll() {
        if (!playingAll) {
            stopAllButton?.visibility = View.VISIBLE
            playAllButton?.visibility = View.GONE
            if (mWorkouts.size > 0) {
                mWorkouts.get(0).isCount = true

                var playingIndiv = false

                for (i in mWorkouts.indices) {
                    if (mWorkouts.get(i).isPlaying!!) {
                        playingIndiv = true
                    }
                }

                if (!playingIndiv) {
                    playingAll = true
                    workoutAdapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "First Stop Playing Individual", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            playingAll = false
            countdownPlayAll?.cancel()
            for (i in mWorkouts.indices) {
                mWorkouts.get(i).isCount = false
                mWorkouts.get(i).isDefaultState = true
            }
            workoutAdapter?.notifyDataSetChanged()
        }
    }

    fun stopPlayingAll() {
        stopAllButton?.visibility = View.GONE
        playAllButton?.visibility = View.VISIBLE
        playingAll = false
        countdownPlayAll?.cancel()
        for (i in mWorkouts.indices) {
            mWorkouts.get(i).isCount = false
            mWorkouts.get(i).isDefaultState = true
        }
        workoutAdapter?.notifyDataSetChanged()
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
