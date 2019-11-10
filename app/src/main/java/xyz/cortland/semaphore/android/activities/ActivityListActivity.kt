package xyz.cortland.semaphore.android.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView

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

        setupView()

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

    }

    override fun onPause() {
        super.onPause()

    }

    @Subscribe(sticky = true)
    fun onEvent(event: CountDownEvent) {
        currentCountDownTimer = event.countdownTimer
    }

    fun setupView() {

        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_add_activity, R.drawable.ic_add_black_24dp).setLabel("Add Activity").create()
        )
        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_import_activities, R.drawable.ic_file_upload_white_24dp).setLabel("Import Activities").create()
        )
        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_activities_history, R.drawable.ic_history_white_24dp).setLabel("Play All History").create()
        )
        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_export_activities, R.drawable.ic_file_download_white_24dp).setLabel("Export Activities").create()
        )
        speedDialView.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_create_group_of_activities, R.drawable.ic_add_black_24dp).setLabel("Add Activities Group").create()
        )

        speedDialView.setOnActionSelectedListener( SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_add_activity -> {
                    val newActivityFragment = NewActivityDialogFragment()
                    newActivityFragment.show(supportFragmentManager, "newActivity")
                    return@OnActionSelectedListener true
                }

                R.id.fab_import_activities -> {
                    Toast.makeText(this, "Implement Activities Importing", Toast.LENGTH_SHORT).show()
                    //speedDialView.close()
                    return@OnActionSelectedListener true
                }

                R.id.fab_activities_history -> {
                    Toast.makeText(this, "Implement Activities History", Toast.LENGTH_SHORT).show()
                    //speedDialView.close()
                    return@OnActionSelectedListener true
                }

                R.id.fab_export_activities -> {
                    Toast.makeText(this, "Implement Activities Exporting", Toast.LENGTH_SHORT).show()
                    //speedDialView.close()
                    return@OnActionSelectedListener true
                }

                R.id.fab_create_group_of_activities -> {
                    Toast.makeText(this, "Implement Activities Grouping", Toast.LENGTH_SHORT).show()
                    //speedDialView.close()
                    return@OnActionSelectedListener true
                }

            }
            false
        })

    }

    override fun onSaveClick(dialog: DialogFragment, activityEntity: ActivityEntity) {
        if (prefs.isOptionEditingActivity) {
            //updateActivityItem(prefs.optionEditSelectedActivityId, activityEntity)
            prefs.removePreferences(IS_OPTION_EDITING_ACTIVITY) // Remove editing activityEntity from preferences
            prefs.removePreferences(OPTION_EDITING_ACTIVITY_ID) // Remove selected activityEntity id from preferences
            AppExecutors.getInstance().diskIO().execute {
                semaphoreDB!!.activityDao().updateActivityEntity(activityEntity)
            }
            speedDialView.close()
            dialog.dismiss()
        } else {
            AppExecutors.getInstance().diskIO().execute {
                semaphoreDB!!.activityDao().insertActivityEntity(activityEntity)
            }
            speedDialView.close()
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            IS_PLAYING_ALL_IN_ORDER_ACTIVITIES -> {
                if (prefs.isPlayingAllInOrderActivities == true) {
                    speedDialView.visibility = View.GONE
                } else {
                    speedDialView.visibility = View.VISIBLE
                }
            }
            IS_PLAYING_ALL_ACTIVITIES -> {
                if (prefs.isPlayingAllActivities == true) {
                    // Hide Add ActivityEntity Button
                    speedDialView.visibility = View.GONE
                } else {
                    // Show Add ActivityEntity Button
                    speedDialView.visibility = View.VISIBLE
                }
            }
        }
    }

}
