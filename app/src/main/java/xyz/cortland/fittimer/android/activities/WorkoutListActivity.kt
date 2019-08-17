package xyz.cortland.fittimer.android.activities

import android.animation.LayoutTransition
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat

import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list.*
import kotlinx.android.synthetic.main.workout_list_content.view.*
import xyz.cortland.fittimer.android.FitTimer
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.adapter.WorkoutRecyclerViewAdapter
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.fragments.NewWorkoutDialogFragment

import xyz.cortland.fittimer.android.model.WorkoutModel
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

    var workoutAdapter: WorkoutRecyclerViewAdapter? = null

    var dbHandler: WorkoutDatabase? = null

    val mWorkouts: ArrayList<WorkoutModel> = ArrayList<WorkoutModel>()

    var playingAll: Boolean by Delegates.observable(false) { _, _, _ ->
        validatePlayAll()
    }

    var playAllButton: Button? = null

    var stopAllButton: Button? = null

    var mGlobalPreferences: GlobalPreferences? = null

    var itemTouchHelper: ItemTouchHelper? = null

    var semaphore: Semaphore? = null

    var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        dbHandler = WorkoutDatabase(this, null)

        mGlobalPreferences = GlobalPreferences(this)

        mWorkouts.addAll(dbHandler!!.allWorkoutsList())
        workoutAdapter = WorkoutRecyclerViewAdapter(this, mWorkouts)
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
        dbHandler?.close()
        workoutAdapter?.stopAllWorkouts()
        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    override fun onResume() {
        super.onResume()

        // Used for Editing Workouts
        if (mGlobalPreferences!!.isWorkoutModified()) {
            mWorkouts.clear()
            item_list.invalidate()
            mWorkouts.addAll(dbHandler!!.allWorkoutsList())
            workoutAdapter!!.notifyDataSetChanged()
            validateWorkoutCount()
            mGlobalPreferences!!.setWorkoutModified(edited = false)
        } else {
            return
        }

        // TODO: Implement shared textToSpeech
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val language = textToSpeech?.setLanguage(Locale(FitTimer.applicationContext().mGlobalPreferences?.getSpeechLanguage()))
                if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language Not Supported.")
                } else {
                    println("Language Supported.")
                }
            }
        })
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
                val holder = item_list.getChildViewHolder(item_list.getChildAt(i)) as WorkoutRecyclerViewAdapter.ViewHolder?
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

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val language = textToSpeech?.setLanguage(Locale(FitTimer.applicationContext().mGlobalPreferences?.getSpeechLanguage()))
                if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language Not Supported.")
                } else {
                    println("Language Supported.")
                }
            }
        })

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

    override fun onSaveClick(dialog: DialogFragment, workout: WorkoutModel) {
        dbHandler!!.addWorkout(workout)
        mWorkouts.clear()
        mWorkouts.addAll(dbHandler!!.allWorkoutsList())
        workoutAdapter?.notifyDataSetChanged()
        validateWorkoutCount()
        dialog.dismiss()
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

    // TODO: Need to do better
    fun showPlayButtons() {
        for (i in 0 until item_list.childCount) {
            val playButton = item_list.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<Button>(R.id.single_play_button)
            val stopButton = item_list.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<Button>(R.id.single_stop_button)
            playButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
            workoutAdapter?.notifyItemChanged(i)
        }
    }

    fun hidePlayButtons() {
        for (i in 0 until item_list.childCount) {
            val playButton = item_list.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<Button>(R.id.single_play_button)
            val stopButton = item_list.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<Button>(R.id.single_stop_button)
            playButton.visibility = View.GONE
            stopButton.visibility = View.GONE
        }
    }

}
