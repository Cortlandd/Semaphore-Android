package xyz.cortland.fittimer.android

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list_content.view.*
import kotlinx.android.synthetic.main.workout_list.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

import xyz.cortland.fittimer.android.model.Workout
import java.lang.Exception


/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [WorkoutDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class WorkoutListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

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

        setupRecyclerView(item_list)

        play_all_button.setOnClickListener {


            for (i in 0 until item_list.childCount) {

                var seconds = item_list.findViewHolderForAdapterPosition(i)!!.itemView.findViewById(R.id.seconds) as TextView

                val s = seconds.text.toString()
                val secondsRemaining = s.toInt() * 1000
                println("Seconds Remaining: ${secondsRemaining.toLong()}")
                val countdownTimer = object: CountDownTimer(secondsRemaining.toLong(), 1000) {

                    override fun onFinish() {
                        seconds.text = s
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        val sec = millisUntilFinished / 1000
                        // Physically able to see the countdown happen
                        seconds.text = sec.toString()
                    }
                }
                countdownTimer.start()
            }
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = WorkoutRecyclerViewAdapter(this, WorkoutContent.ITEMS, false)
    }

    class WorkoutRecyclerViewAdapter(private val parentActivity: WorkoutListActivity, private val values: List<Workout>, private val twoPane: Boolean) : RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as Workout
                if (twoPane) {
                    val fragment = WorkoutDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(WorkoutDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, WorkoutDetailActivity::class.java).apply {
                        putExtra(WorkoutDetailFragment.ARG_ITEM_ID, item.id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.workout_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.id
            holder.workoutView.text = item.workout
            holder.secondsView.text = item.seconds

            holder.playButton.setOnClickListener {

                val s = holder.secondsView.text.toString()
                val secondsRemaining = s.toInt() * 1000
                println("Seconds Remaining: ${secondsRemaining.toLong()}")
                val countdownTimer = object: CountDownTimer(secondsRemaining.toLong(), 1000) {

                    override fun onFinish() {
                        holder.secondsView.text = s
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        var seconds = millisUntilFinished / 1000
                        holder.secondsView.text = seconds.toString()
                    }
                }
                countdownTimer.start()

            }

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var idView: TextView = view.id_text
            var secondsView: TextView = view.seconds
            var workoutView: TextView = view.workout
            var playButton: Button = view.single_play_button
        }

        fun playAll() {
            println("Play button")
        }
    }


}
