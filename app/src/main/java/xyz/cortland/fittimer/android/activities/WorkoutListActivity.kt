package xyz.cortland.fittimer.android.activities

import android.animation.LayoutTransition
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.core.app.NotificationCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list.*
import xyz.cortland.fittimer.android.FitTimer
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.adapter.WorkoutAdapter
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.extensions.dbHandler
import xyz.cortland.fittimer.android.fragments.NewWorkoutDialogFragment
import xyz.cortland.fittimer.android.helpers.*

import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.utils.GlobalPreferences
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.properties.Delegates


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

    var workoutAdapter: WorkoutAdapter? = null

    val mWorkouts: ArrayList<Workout> = ArrayList<Workout>()

    var playingAll: Boolean by Delegates.observable(false) { _, _, _ ->
        validatePlayAll()
    }

    var isPaused: Boolean? = false

    var playAllButton: Button? = null

    var stopAllButton: Button? = null

    var itemTouchHelper: ItemTouchHelper? = null

    var semaphore: Semaphore? = null

    var notificationManager: NotificationManager? = null
    var notificationBuilder: NotificationCompat.Builder? = null

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

    override fun onDestroy() {
        super.onDestroy()
        dbHandler.close()
        workoutAdapter?.stopAllWorkouts()
    }

    override fun onResume() {
        super.onResume()

        isPaused = false

        if (playingAll) {
            notificationManager?.cancel(WORKOUT_FINISHED_ID)
        }

        // Used for Editing Workouts
        if (prefs!!.workoutModified) {
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

        if (playingAll) {
            val currentWorkout = prefs?.currentPlayingAllWorkoutPosition
            createNotification(currentWorkout!!)
        }
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

            playingAll = true

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
            stopPlayingAll()
        }

        fab.setOnClickListener {
            val newWorkoutFragment = NewWorkoutDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

        // TODO: Doesn't work but ill keep it in just in case
        fab_placeholder.setOnClickListener {
            val newWorkoutFragment = NewWorkoutDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

        workout_list_activity.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

    }

    private fun validatePlayAll() {
        if (playingAll == true) {

            // Hide Add Workout Button
            fab.hide()

            // Disable swiping while playing
            itemTouchHelper!!.attachToRecyclerView(null)

            stopAllButton?.visibility = View.VISIBLE
            playAllButton?.visibility = View.GONE

            hidePlayButtons()

        } else {

            // Update UI Buttons
            stopAllButton?.visibility = View.GONE
            playAllButton?.visibility = View.VISIBLE

            fab.show()

            itemTouchHelper!!.attachToRecyclerView(item_list)

            showPlayButtons()

            mWorkouts.forEach {
                it.isDefaultState = true
            }

            workoutAdapter!!.stopAllWorkouts()
            workoutAdapter!!.notifyDataSetChanged()

        }
    }

    fun stopPlayingAll() {

        // Change state
        playingAll = false
    }

    override fun onSaveClick(dialog: DialogFragment, workout: Workout) {
        if (prefs!!.editingLongPressWorkout) {
            updateWorkoutItem(prefs.longPressWorkoutId, workout)
            FitTimer.applicationContext().preferences!!.removePreferences(EDITING_WORKOUT) // Remove editing workout from preferences
            FitTimer.applicationContext().preferences!!.removePreferences(LONGPRESS_WORKOUT_ID) // Remove longpressed workout id from preferences
            // TODO: Shouldn't have to clear all then add again
            mWorkouts.clear()
            mWorkouts.addAll(dbHandler!!.allWorkoutsList())
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
                if (playingAll) {
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

    fun createNotification(currentWorkout: Int) {

        val workout = mWorkouts.get(currentWorkout)
        val intent = Intent(applicationContext, this::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(WORKOUT_CHANNEL, "workout_channel", NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
                notificationManager!!.createNotificationChannel(this)
            }
        }

        var numMessages = 0

        // TODO: Why do I need a Channel
        notificationBuilder = NotificationCompat.Builder(this, WORKOUT_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(workout.workoutName)
            .setContentText("${prefs?.currentPlayingAllRemainingTime} seconds remaining.")
            .setNumber(++numMessages)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setShowWhen(false)
            .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager!!.notify(WORKOUT_FINISHED_ID, notificationBuilder!!.build())

    }

    fun updateNotification(text: String, workoutName: String?) {
        notificationBuilder!!
            .setContentTitle(workoutName)
            .setContentText(text)
            .setSound(null)
        notificationManager!!.notify(WORKOUT_FINISHED_ID, notificationBuilder!!.build())
    }

    // TODO: Need to do better
    fun showPlayButtons() {
        for (i in 0 until item_list.childCount) {
            val playButton = item_list.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<FloatingActionButton>(R.id.single_play_button)
            playButton.show()
            workoutAdapter?.notifyItemChanged(i)
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
        if (prefs!!.currentImageRemoved) {
            values.putNull(WorkoutDatabase.COLUMN_WORKOUTIMAGE)
            prefs.currentImageRemoved = false
        } else {
            values.put(WorkoutDatabase.COLUMN_WORKOUTIMAGE, workout.workoutImage)
        }
        values.put(WorkoutDatabase.COLUMN_WORKOUTSPEECH, workout.workoutSpeech)
        db.update(WorkoutDatabase.TABLE_NAME, values, WorkoutDatabase.COLUMN_ID + "=" + id, null)
        db.close()
    }

}
