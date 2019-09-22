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

import kotlinx.android.synthetic.main.activity_activity_list.*
import kotlinx.android.synthetic.main.activity_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.SemaphoreApp
import xyz.cortland.fittimer.android.adapter.ActivityAdapter
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.ActivityDatabase
import xyz.cortland.fittimer.android.extensions.dbHandler
import xyz.cortland.fittimer.android.extensions.hideTimerNotification
import xyz.cortland.fittimer.android.fragments.NewActivityDialogFragment
import xyz.cortland.fittimer.android.helpers.*

import xyz.cortland.fittimer.android.model.ActivityModel
import xyz.cortland.fittimer.android.receivers.CountDownEvent
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread


/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ActivityDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ActivityListActivity : AppCompatActivity(), NewActivityDialogFragment.NewActivityDialogListener, SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    //private var twoPane: Boolean = false

    var activityAdapter: ActivityAdapter? = null
    var currentCountDownTimer: CountDownTimer? = null
    var itemTouchHelper: ItemTouchHelper? = null
    var semaphore: Semaphore? = null

    val mActivityModels: ArrayList<ActivityModel> = ArrayList<ActivityModel>()
    var isPaused: Boolean? = false

    var playAllButton: Button? = null
    var stopAllButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        mActivityModels.addAll(dbHandler.allActivitiesList())
        activityAdapter = ActivityAdapter(this, mActivityModels)
        item_list.adapter = activityAdapter

        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val image = mActivityModels.get(position).activityImage
                mActivityModels.removeAt(position)
                item_list!!.adapter?.notifyItemRemoved(position)
                removeActivityItem(viewHolder.itemView.id)
                if (image != null) {
                    val df = File(image)
                    df.delete()
                }
                validateActivityCount()
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
        activityAdapter?.stopAllActivities()
        prefs.isPlayingAllActivities = false
        prefs.mSharedPreferences?.unregisterOnSharedPreferenceChangeListener(this) // Register prefs for change listening in this Activity
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()

        isPaused = false

        hideTimerNotification()

        if (prefs.isPlayingAllActivities) {
            EventBus.getDefault().removeAllStickyEvents()
        }

        // Used for Editing Workouts
        if (prefs.activityModified) {
            mActivityModels.clear()
            item_list.invalidate()
            mActivityModels.addAll(dbHandler.allActivitiesList())
            activityAdapter!!.notifyDataSetChanged()
            validateActivityCount()
            prefs.activityModified = false
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
    fun removeActivityItem(id: Int) {
        val dbHelper = ActivityDatabase(this, null)
        val db = dbHelper.writableDatabase
        db.delete(ActivityDatabase.TABLE_NAME, ActivityDatabase.COLUMN_ID + "=" + id, null)
        db.close()
    }

    private fun setupView() {

        playAllButton = findViewById<Button>(R.id.play_all_button)
        stopAllButton = findViewById<Button>(R.id.stop_all_button)

        validateActivityCount()

        playAllButton!!.setOnClickListener {

            prefs.isPlayingAllActivities = true

            semaphore?.drainPermits() // Just in case
            semaphore = Semaphore(1)

            for (i in mActivityModels.indices) {
                val holder = item_list.getChildViewHolder(item_list.getChildAt(i)) as ActivityAdapter.ViewHolder?
                holder!!.itemView.isEnabled = false
                thread {
                    semaphore!!.acquire(1)
                    println("Aquired")
                    activityAdapter?.play(holder, i, semaphore!!)
                }
            }

        }

        stopAllButton!!.setOnClickListener {
            prefs.isPlayingAllActivities = false
        }

        fab.setOnClickListener {
            val newWorkoutFragment = NewActivityDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

        fab_placeholder.setOnClickListener {
            val newWorkoutFragment = NewActivityDialogFragment()
            newWorkoutFragment.show(supportFragmentManager, "newWorkout")
        }

        activity_list_activity.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

    }

    override fun onSaveClick(dialog: DialogFragment, activityModel: ActivityModel) {
        if (prefs.editingLongPressActivity) {
            updateActivityItem(prefs.longPressActivityId, activityModel)
            SemaphoreApp.applicationContext().preferences!!.removePreferences(EDITING_ACTIVITY) // Remove editing activityModel from preferences
            SemaphoreApp.applicationContext().preferences!!.removePreferences(LONGPRESS_ACTIVITY_ID) // Remove longpressed activityModel id from preferences
            // TODO: Shouldn't have to clear all then add again. Also need animations for insert and remove
            mActivityModels.clear()
            mActivityModels.addAll(dbHandler.allActivitiesList())
            activityAdapter?.notifyDataSetChanged()
            validateActivityCount()
            dialog.dismiss()
        } else {
            dbHandler.addActivity(activityModel)
            mActivityModels.clear()
            mActivityModels.addAll(dbHandler.allActivitiesList())
            activityAdapter!!.notifyDataSetChanged()
            validateActivityCount()
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
                if (prefs.isPlayingAllActivities) {
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

    fun validateActivityCount() {

        if (mActivityModels.size == 0 || mActivityModels.size == null) {
            no_activities_message.visibility = View.VISIBLE
        } else {
            no_activities_message.visibility = View.GONE
        }

        if (mActivityModels.size <= 1 || mActivityModels == null) {
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
     * @param activityModel: The activityModel to be updated
     */
    private fun updateActivityItem(id: Int, activityModel: ActivityModel) {
        val dbHelper = ActivityDatabase(this, null)
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(ActivityDatabase.COLUMN_SECONDS, activityModel.seconds)
        values.put(ActivityDatabase.COLUMN_ACTIVITY, activityModel.activityName)
        if (prefs.currentImageRemoved) {
            values.putNull(ActivityDatabase.COLUMN_ACTIVITYIMAGE)
            prefs.currentImageRemoved = false
        } else {
            values.put(ActivityDatabase.COLUMN_ACTIVITYIMAGE, activityModel.activityImage)
        }
        values.put(ActivityDatabase.COLUMN_ACTIVITYSPEECH, activityModel.activitySpeech)
        db.update(ActivityDatabase.TABLE_NAME, values, ActivityDatabase.COLUMN_ID + "=" + id, null)
        db.close()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            IS_PLAYING_ALL_ACTIVITIES -> {
                if (prefs.isPlayingAllActivities) {
                    // Hide Add ActivityModel Button
                    fab.hide()
                    // Disable swiping while playing
                    itemTouchHelper!!.attachToRecyclerView(null)
                    // Update Play All and Stop All buttons
                    stopAllButton!!.visibility = View.VISIBLE
                    playAllButton!!.visibility = View.GONE

                    hidePlayButtons()
                } else {
                    // Update Play All and Stop All buttons
                    stopAllButton!!.visibility = View.GONE
                    playAllButton!!.visibility = View.VISIBLE
                    // Show Add ActivityModel Button
                    fab.show()
                    // Enable swiping while playing
                    itemTouchHelper!!.attachToRecyclerView(item_list)

                    showPlayButtons()

                    semaphore?.drainPermits()

                    activityAdapter?.stopAllActivities()
                }
            }
        }
    }

}
