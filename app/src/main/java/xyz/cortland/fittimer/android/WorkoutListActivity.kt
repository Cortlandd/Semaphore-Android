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
import android.widget.Chronometer
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_workout_list.*
import kotlinx.android.synthetic.main.workout_list_content.view.*
import kotlinx.android.synthetic.main.workout_list.*
import kotlinx.android.synthetic.main.workout_list_content.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

import xyz.cortland.fittimer.android.model.Workout

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



    //var semaphores = ArrayList<Semaphore>(allItems.size)

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

        var allItems = WorkoutContent.ITEMS

        var semaphores = ArrayList<Semaphore>(allItems.size)

        // Start the first workout with permission from semaphore`
        //semaphores[0] = Semaphore(1)

        play_all_button.setOnClickListener { view ->

            for (cell in allItems) {

                var secondsRemaining = cell.seconds.toInt()
                secondsRemaining -= 1
                id_text.text = secondsRemaining.toString()

                if (secondsRemaining == 0) {
                    break
                }

            }
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = WorkoutRecyclerViewAdapter(this, WorkoutContent.ITEMS, false)
    }

    class WorkoutRecyclerViewAdapter(private val parentActivity: WorkoutListActivity, private val values: List<Workout>, private val twoPane: Boolean) :
        RecyclerView.Adapter<WorkoutRecyclerViewAdapter.ViewHolder>() {

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

                var s = holder.secondsView.text.toString()
                var secondsRemaining = s.toInt() * 1000
                println("Seconds Remaining: ${secondsRemaining.toLong()}")
                var countdownTimer = object: CountDownTimer(secondsRemaining.toLong(), 1000) {

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
    }

    private class WorkerThread(private val semaphores: Array<Semaphore>, private val index: Int) : Thread() {

        private val condition: Condition
        private var progress: Int = 0
        private var isSuspended = false
        private val lock: ReentrantLock

        init {
            lock = ReentrantLock()
            condition = lock.newCondition()
        }

        /**
         * We use Semaphores here to coordinate the threads because the Semaphore in java is not 'fully-bracketed',
         * which means the thread to release a permit does not have to be the one that has acquired
         * the permit in the first place.
         * We can utilise this feature of Semaphore to let one thread to release a permit for the next thread.
         */
        override fun run() {
            val currentSemaphore = semaphores[index]
            val nextSemaphore = semaphores[(index + 1) % semaphores.size]

            try {
                while (true) {
                    currentSemaphore.acquire()

                    lock.lock()
                    while (isSuspended) {
                        condition.await()
                    }
                    lock.unlock()
                    //Thread.sleep(300) // we use a sleep call to mock some lengthy work.
//                    if (progressBarHandler != null) {
//                        val message = progressBarHandler!!.obtainMessage()
//                        message.arg1 = (progress += 10)
//                        progressBarHandler!!.sendMessage(message)
//                    }

                    nextSemaphore.release()

                    if (progress == 100) {
                        progress = -10
                    }
                }

            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }

        fun resumePlay() {
            lock.lock()
            isSuspended = false
            condition.signal()
            lock.unlock()
        }

        fun pausePlay() {
            lock.lock()
            isSuspended = true
            lock.unlock()
        }
    }

}
