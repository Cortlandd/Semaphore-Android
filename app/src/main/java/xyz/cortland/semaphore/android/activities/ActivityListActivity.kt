package xyz.cortland.semaphore.android.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_activity_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import xyz.cortland.semaphore.android.R
import xyz.cortland.semaphore.android.custom.CountDownTimer
import xyz.cortland.semaphore.android.database.AppExecutors
import xyz.cortland.semaphore.android.extensions.hideTimerNotification
import xyz.cortland.semaphore.android.extensions.semaphoreDB
import xyz.cortland.semaphore.android.fragments.ActivityFragment
import xyz.cortland.semaphore.android.fragments.NewActivityDialogFragment
import xyz.cortland.semaphore.android.helpers.*
import xyz.cortland.semaphore.android.model.ActivityEntity

import xyz.cortland.semaphore.android.receivers.CountDownEvent


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

    var currentCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        // Show ActivityFragment
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.frameLayout, ActivityFragment(), "ActivityFragment")
                .commit()
        }

        fab.setOnClickListener {
            val newActivityFragment = NewActivityDialogFragment()
            newActivityFragment.show(supportFragmentManager, "newActivity")
        }

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideTimerNotification()
        EventBus.getDefault().unregister(this)
        EventBus.getDefault().removeAllStickyEvents()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()

        hideTimerNotification()

        if (prefs.isPlayingAllInOrderActivities) {
            EventBus.getDefault().removeAllStickyEvents()
        }

        // Used for Editing Activities
//        if (prefs.activityModified) {
//            mActivityModels.clear()
//            item_list.invalidate()
//            mActivityModels.addAll(dbHandler.allActivitiesList())
//            activityAdapter!!.notifyDataSetChanged()
//            validateActivityCount()
//            prefs.activityModified = false
//        } else {
//            return
//        }

    }

    override fun onPause() {
        super.onPause()

    }

    @Subscribe(sticky = true)
    fun onEvent(event: CountDownEvent) {
        currentCountDownTimer = event.countdownTimer
    }

    override fun onSaveClick(dialog: DialogFragment, activityEntity: ActivityEntity) {
        if (prefs.isOptionEditingActivity) {
            //updateActivityItem(prefs.optionEditSelectedActivityId, activityEntity)
            prefs.removePreferences(IS_OPTION_EDITING_ACTIVITY) // Remove editing activityEntity from preferences
            prefs.removePreferences(OPTION_EDITING_ACTIVITY_ID) // Remove selected activityEntity id from preferences
            AppExecutors.getInstance().diskIO().execute {
                semaphoreDB!!.activityDao().updateActivityEntity(activityEntity)
            }
            dialog.dismiss()
        } else {
            AppExecutors.getInstance().diskIO().execute {
                semaphoreDB!!.activityDao().insertActivityEntity(activityEntity)
            }
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
                if (prefs.isPlayingAllActivities || prefs.isPlayingAllInOrderActivities) {
                    Toast.makeText(this, "Stop All Activities before changing Settings.", Toast.LENGTH_SHORT).show()
                } else {
                    val i = Intent(this, SettingsActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                }
            }
        }

        return true
    }

    /**
     * Used to update long pressed Activities
     *
     * @param id: The id of the Activity
     * @param activityEntity: The activityEntity to be updated
     */
//    private fun updateActivityItem(id: Int, activityEntity: ActivityEntity) {
//        val dbHelper = ActivityDatabase(this, null)
//        val db = dbHelper.writableDatabase
//        val values = ContentValues()
//        values.put(ActivityDatabase.COLUMN_HOURS, activityEntity.hours)
//        values.put(ActivityDatabase.COLUMN_MINUTES, activityEntity.minutes)
//        values.put(ActivityDatabase.COLUMN_SECONDS, activityEntity.seconds)
//        values.put(ActivityDatabase.COLUMN_ACTIVITY, activityEntity.activityName)
//        if (prefs.currentImageRemoved) {
//            values.putNull(ActivityDatabase.COLUMN_ACTIVITYIMAGE)
//            prefs.currentImageRemoved = false
//        } else {
//            values.put(ActivityDatabase.COLUMN_ACTIVITYIMAGE, activityEntity.activityImage)
//        }
//        values.put(ActivityDatabase.COLUMN_ACTIVITYSPEECH, activityEntity.activitySpeech)
//        db.update(ActivityDatabase.TABLE_NAME, values, ActivityDatabase.COLUMN_ID + "=" + id, null)
//        db.close()
//    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            IS_PLAYING_ALL_IN_ORDER_ACTIVITIES -> {
                if (prefs.isPlayingAllInOrderActivities) {
                    fab.hide()
                } else {
                    fab.show()
                }
            }
            IS_PLAYING_ALL_ACTIVITIES -> {
                if (prefs.isPlayingAllActivities) {
                    // Hide Add ActivityEntity Button
                    fab.hide()
                } else {
                    // Show Add ActivityEntity Button
                    fab.show()
                }
            }
        }
    }

}
