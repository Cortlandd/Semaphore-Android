package xyz.cortland.fittimer.android.activities

import android.content.ContentValues
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_workout_detail.*
import xyz.cortland.fittimer.android.FitTimer
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.extensions.dbHandler
import xyz.cortland.fittimer.android.fragments.NewWorkoutDialogFragment
import xyz.cortland.fittimer.android.fragments.WorkoutDetailFragment
import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.utils.GlobalPreferences

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [WorkoutListActivity].
 */
class WorkoutDetailActivity : AppCompatActivity(), NewWorkoutDialogFragment.NewWorkoutDialogListener {

    var workout: Workout? = null
    var workoutId: Int? = null

    var fragment: WorkoutDetailFragment? = null

    var prefs: GlobalPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_detail)
        setSupportActionBar(detail_toolbar)

        prefs = FitTimer.applicationContext().preferences

        workout = intent.getParcelableExtra("arg_parcel_workout")
        workoutId = intent.getIntExtra("arg_workout_id", 0)

        fab_edit_workout.setOnClickListener { view ->

            if (workout != null) {
                val newWorkoutFragment = NewWorkoutDialogFragment.newInstance(workout!!, workoutId!!)
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
            fragment = WorkoutDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WorkoutDetailFragment.ARG_ITEM_ID, intent.getParcelableExtra("arg_parcel_workout"))
                    putInt(WorkoutDetailFragment.ARG_WORKOUT_ID, intent.getIntExtra("arg_workout_id", 0))
                }
            }

            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, fragment!!)
                .commit()
        }
    }

    override fun onSaveClick(dialog: DialogFragment, workout: Workout) {
        updateWorkoutItem(workoutId!!, workout)
        dialog.dismiss()
        supportFinishAfterTransition()
    }

    /**
     * Used to remove swiped Workouts
     *
     * @param id: The id of the book
     * @param workout: The workout to be updated
     */
    private fun updateWorkoutItem(id: Int, workout: Workout) {
        val db = dbHandler.writableDatabase
        val values = ContentValues()
        values.put(WorkoutDatabase.COLUMN_HOURS, workout.hours)
        values.put(WorkoutDatabase.COLUMN_MINUTES, workout.minutes)
        values.put(WorkoutDatabase.COLUMN_SECONDS, workout.seconds)
        values.put(WorkoutDatabase.COLUMN_WORKOUT, workout.workoutName)
        if (prefs!!.currentImageRemoved) {
            values.putNull(WorkoutDatabase.COLUMN_WORKOUTIMAGE)
            prefs!!.currentImageRemoved = false
        } else {
            values.put(WorkoutDatabase.COLUMN_WORKOUTIMAGE, workout.workoutImage)
        }
        values.put(WorkoutDatabase.COLUMN_WORKOUTSPEECH, workout.workoutSpeech)
        db.update(WorkoutDatabase.TABLE_NAME, values, WorkoutDatabase.COLUMN_ID + "=" + id, null)
        db.close()
    }

    override fun onCancelClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //navigateUpTo(Intent(this, WorkoutListActivity::class.java))
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

                //navigateUpTo(Intent(this, WorkoutListActivity::class.java))
                supportFinishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
