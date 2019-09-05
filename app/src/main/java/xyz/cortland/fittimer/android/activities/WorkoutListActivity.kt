package xyz.cortland.fittimer.android.activities

import android.animation.LayoutTransition
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import xyz.cortland.fittimer.android.FitTimer
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.adapter.WorkoutAdapter
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.extensions.dbHandler
import xyz.cortland.fittimer.android.extensions.hideTimerNotification
import xyz.cortland.fittimer.android.fragments.NewWorkoutDialogFragment
import xyz.cortland.fittimer.android.helpers.*

import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.receivers.CountDownEvent
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread


/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [WorkoutDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class WorkoutListActivity : AppCompatActivity(), NewWorkoutDialogFragment.NewWorkoutDialogListener, SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    //private var twoPane: Boolean = false

    var workoutAdapter: WorkoutAdapter? = null
    var currentCountDownTimer: CountDownTimer? = null
    var itemTouchHelper: ItemTouchHelper? = null
    var semaphore: Semaphore? = null

    val mWorkouts: ArrayList<Workout> = ArrayList<Workout>()
    var isPaused: Boolean? = false

    var playAllButton: Button? = null
    var stopAllButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        mWorkouts.addAll(dbHandler.allWorkoutsList())
        workoutAdapter = WorkoutAdapter(this, mWorkouts)
        item_list.adapter = workoutAdapter

        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val image = mWorkouts.get(position).workoutImage
                mWorkouts.removeAt(position)
                item_list!!.adapter?.notifyItemRemoved(position)
                removeWorkoutItem(viewHolder.itemView.id)
                if (image != null) {
                    val df = File(image)
                    df.delete()
                }
                validateWorkoutCount()
            }

        }

        itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper!!.attachToRecyclerView(item_list)

        setupView()

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        prefs.mSharedPreferences?.registerOnSharedPreferenceChangeListener(this) // Register prefs for change listening in this Activity
    }

    override fun onDestroy() {
        super.onDestroy()
        hideTimerNotification()
        EventBus.getDefault().unregister(this)
        EventBus.getDefault().removeAllStickyEvents()
        dbHandler.close()
        workoutAdapter?.stopAllWorkouts()
        prefs.isPlayingAllWorkouts = false
        prefs.mSharedPreferences?.unregisterOnSharedPreferenceChangeListener(this) // Register prefs for change listening in this Activity
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()

        isPaused = false

        hideTimerNotification()

        if (prefs.isPlayingAllWorkouts) {
            EventBus.getDefault().removeAllStickyEvents()
        }

        // Used for Editing Workouts
        if (prefs.workoutModified) {
            mWorkouts.clear()
            item_list.invalidate()
            mWorkouts.addAll(dbHandler.allWorkoutsList())
            workoutAdapter!!.notifyDataSetChanged()
            validateWorkoutCount()
            prefs.workoutModified = false
        } else {
            return
        }

    }

    override fun onPause() {
        super.onPause()

        isPaused = true

    }

    @Subscribe(sticky = true)
    fun onEvent(event: CountDownEvent) {
        currentCountDownTimer = event.countdownTimer
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

    private fun setupView() {

        if (mWorkouts.size == 0 || mWorkouts.size == null) {
            no_workouts_message.visibility = View.VISIBLE
        } else {
            no_workouts_message.visibility = View.GONE
        }

        playAllButton = findViewById<Button>(R.id.play_all_button)
        stopAllButton = findViewById<Button>(R.id.stop_all_button)

        validateWorkoutCount()

        playAllButton!!.setOnClickListener {

            prefs.isPlayingAllWorkouts = true

            semaphore = Semaphore(1)

            for (i in mWorkouts.indices) {
                val holder = item_list.getChildViewHolder(item_list.getChildAt(i)) as WorkoutAdapter.ViewHolder?
                holder!!.itemView.isEnabled = false
                thread {
                    semaphore!!.acquire(1)
                    println("Aquired")
                    workoutAdapter?.play(holder, i, semaphore!!)
                }
            }

        }

        stopAllButton!!.setOnClickListener {
            prefs.isPlayingAllWorkouts = false
        }

        fab.setOnClickListener {
            val newWorkoutFragment = NewWorkoutDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

        fab_placeholder.setOnClickListener {
            val newWorkoutFragment = NewWorkoutDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

        workout_list_activity.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

    }

    override fun onSaveClick(dialog: DialogFragment, workout: Workout) {
        if (prefs.editingLongPressWorkout) {
            updateWorkoutItem(prefs.longPressWorkoutId, workout)
            FitTimer.applicationContext().preferences!!.removePreferences(EDITING_WORKOUT) // Remove editing workout from preferences
            FitTimer.applicationContext().preferences!!.removePreferences(LONGPRESS_WORKOUT_ID) // Remove longpressed workout id from preferences
            // TODO: Shouldn't have to clear all then add again. Also need animations for insert and remove
            mWorkouts.clear()
            mWorkouts.addAll(dbHandler.allWorkoutsList())
            workoutAdapter?.notifyDataSetChanged()
            validateWorkoutCount()
            dialog.dismiss()
        } else {
            dbHandler.addWorkout(workout)
            mWorkouts.clear()
            mWorkouts.addAll(dbHandler.allWorkoutsList())
            workoutAdapter?.notifyDataSetChanged()
            validateWorkoutCount()
            dialog.dismiss()
        }
    }

    override fun onCancelClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings_menu -> {
                if (prefs.isPlayingAllWorkouts) {
                    Toast.makeText(this, "Stop All Workouts before changing Settings.", Toast.LENGTH_SHORT).show()
                } else {
                    val i = Intent(this, SettingsActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                }
            }
        }

        return true
    }

    fun validateWorkoutCount() {

        if (mWorkouts.size == 0 || mWorkouts.size == null) {
            no_workouts_message.visibility = View.VISIBLE
        } else {
            no_workouts_message.visibility = View.GONE
        }

        if (mWorkouts.size <= 1 || mWorkouts == null) {
            playAllButton!!.visibility = View.GONE
        } else {
            playAllButton!!.visibility = View.VISIBLE
        }
    }

    // TODO: Need to do better
    fun showPlayButtons() {
        for (i in 0 until item_list.childCount) {
            val playButton = item_list.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<FloatingActionButton>(R.id.single_play_button)
            playButton.show()
        }
    }

    fun hidePlayButtons() {
        for (i in 0 until item_list.childCount) {
            val playButton = item_list.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<FloatingActionButton>(R.id.single_play_button)
            playButton.hide()
        }
    }

    /**
     * Used to remove swiped Workouts
     *
     * @param id: The id of the book
     * @param workout: The workout to be updated
     */
    private fun updateWorkoutItem(id: Int, workout: Workout) {
        val dbHelper = WorkoutDatabase(this, null)
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(WorkoutDatabase.COLUMN_SECONDS, workout.seconds)
        values.put(WorkoutDatabase.COLUMN_WORKOUT, workout.workoutName)
        if (prefs.currentImageRemoved) {
            values.putNull(WorkoutDatabase.COLUMN_WORKOUTIMAGE)
            prefs.currentImageRemoved = false
        } else {
            values.put(WorkoutDatabase.COLUMN_WORKOUTIMAGE, workout.workoutImage)
        }
        values.put(WorkoutDatabase.COLUMN_WORKOUTSPEECH, workout.workoutSpeech)
        db.update(WorkoutDatabase.TABLE_NAME, values, WorkoutDatabase.COLUMN_ID + "=" + id, null)
        db.close()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            IS_PLAYING_ALL_WORKOUTS -> {
                if (prefs.isPlayingAllWorkouts) {
                    // Hide Add Workout Button
                    fab.hide()
                    // Disable swiping while playing
                    itemTouchHelper!!.attachToRecyclerView(null)
                    // Update Play All and Stop All buttons
                    stopAllButton?.visibility = View.VISIBLE
                    playAllButton?.visibility = View.GONE

                    hidePlayButtons()
                } else {
                    // Update Play All and Stop All buttons
                    stopAllButton?.visibility = View.GONE
                    playAllButton?.visibility = View.VISIBLE
                    // Show Add Workout Button
                    fab.show()
                    // Enable swiping while playing
                    itemTouchHelper!!.attachToRecyclerView(item_list)

                    showPlayButtons()

                    semaphore?.drainPermits()

                    workoutAdapter?.stopAllWorkouts()
                }
            }
        }
    }

}
