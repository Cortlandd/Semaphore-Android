package xyz.cortland.semaphore.android.activities

import android.content.ContentValues
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_activity_detail.*
import xyz.cortland.semaphore.android.SemaphoreApp
import xyz.cortland.semaphore.android.R
import xyz.cortland.semaphore.android.database.ActivityDatabase
import xyz.cortland.semaphore.android.extensions.dbHandler
import xyz.cortland.semaphore.android.fragments.NewActivityDialogFragment
import xyz.cortland.semaphore.android.fragments.ActivityDetailFragment
import xyz.cortland.semaphore.android.model.ActivityModel
import xyz.cortland.semaphore.android.utils.GlobalPreferences

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ActivityListActivity].
 */
class ActivityDetailActivity : AppCompatActivity(), NewActivityDialogFragment.NewActivityDialogListener {

    var activityModel: ActivityModel? = null
    var activityId: Int? = null

    var fragment: ActivityDetailFragment? = null

    var prefs: GlobalPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_detail)
        setSupportActionBar(detail_toolbar)

        prefs = SemaphoreApp.applicationContext().preferences

        // Retrieved from click inside ActivityAdapter.
        activityModel = intent.getParcelableExtra("arg_parcel_activity")
        activityId = intent.getIntExtra("arg_activity_id", 0)

        fab_edit_activity.setOnClickListener { view ->

            if (activityModel != null) {
                val newWorkoutFragment = NewActivityDialogFragment.newInstance(activityModel!!, activityId!!)
                newWorkoutFragment.show(supportFragmentManager, "ModifyWorkout")
            } else {
                Snackbar.make(view, "Something is wrong", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }

        }

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            fragment = ActivityDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ActivityDetailFragment.ARG_ITEM_ID, intent.getParcelableExtra("arg_parcel_activity"))
                    putInt(ActivityDetailFragment.ARG_ACTIVITY_ID, intent.getIntExtra("arg_activity_id", 0))
                }
            }

            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, fragment!!)
                .commit()
        }
    }

    override fun onSaveClick(dialog: DialogFragment, activityModel: ActivityModel) {
        updateActivityItem(activityId!!, activityModel)
        dialog.dismiss()
        supportFinishAfterTransition()
    }

    /**
     * Used to remove swiped Workouts
     *
     * @param id: The id of the book
     * @param activityModel: The activityModel to be updated
     */
    private fun updateActivityItem(id: Int, activityModel: ActivityModel) {
        val db = dbHandler.writableDatabase
        val values = ContentValues()
        values.put(ActivityDatabase.COLUMN_HOURS, activityModel.hours)
        values.put(ActivityDatabase.COLUMN_MINUTES, activityModel.minutes)
        values.put(ActivityDatabase.COLUMN_SECONDS, activityModel.seconds)
        values.put(ActivityDatabase.COLUMN_ACTIVITY, activityModel.activityName)
        if (prefs!!.currentImageRemoved) {
            values.putNull(ActivityDatabase.COLUMN_ACTIVITYIMAGE)
            prefs!!.currentImageRemoved = false
        } else {
            values.put(ActivityDatabase.COLUMN_ACTIVITYIMAGE, activityModel.activityImage)
        }
        values.put(ActivityDatabase.COLUMN_ACTIVITYSPEECH, activityModel.activitySpeech)
        db.update(ActivityDatabase.TABLE_NAME, values, ActivityDatabase.COLUMN_ID + "=" + id, null)
        db.close()
    }

    override fun onCancelClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //navigateUpTo(Intent(this, ActivityListActivity::class.java))
        supportFinishAfterTransition()
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                //navigateUpTo(Intent(this, ActivityListActivity::class.java))
                supportFinishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
