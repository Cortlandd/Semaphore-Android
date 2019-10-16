package xyz.cortland.semaphore.android.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.jetbrains.anko.doAsync
import xyz.cortland.semaphore.android.R
import xyz.cortland.semaphore.android.adapter.ActivityRecyclerViewAdapter

import xyz.cortland.semaphore.android.helpers.prefs
import xyz.cortland.semaphore.android.model.ActivityEntity
import xyz.cortland.semaphore.android.utils.ActivitiesAdapterObserver
import xyz.cortland.semaphore.android.database.AppExecutors
import xyz.cortland.semaphore.android.database.AppDatabase
import xyz.cortland.semaphore.android.helpers.IS_PLAYING_ALL_ACTIVITIES
import xyz.cortland.semaphore.android.helpers.IS_PLAYING_ALL_IN_ORDER_ACTIVITIES
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 */
class ActivityFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    var adapter: ActivityRecyclerViewAdapter? = null
    var recyclerView: RecyclerView? = null
    var itemTouchHelper: ItemTouchHelper? = null
    var fabPlaceholder: FloatingActionButton? = null

    var semaphoreDB: AppDatabase? = null

    var semaphore: Semaphore? = null

    /* UI */
    var playAllButton: Button? = null
    var playAllInOrderButton: Button? = null
    var stopAllButton: Button? = null

    // TODO: Customize parameters
    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        semaphoreDB = AppDatabase.getAppDataBase(context!!)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_activity_list, container, false)

        recyclerView = view.findViewById(R.id.activities_list)
        fabPlaceholder = view.findViewById(R.id.fab_placeholder)

        retrieveActivityEntities()

        adapter = ActivityRecyclerViewAdapter(context!!)
        recyclerView?.adapter = adapter
        adapter!!.registerAdapterDataObserver(ActivitiesAdapterObserver(recyclerView = recyclerView!!, emptyView = view.findViewById(R.id.empty_activities_layout)))

        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

                val adapter = recyclerView.adapter as ActivityRecyclerViewAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                moveItem(from, to)

                adapter.notifyItemMoved(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute {
                    val position = viewHolder.adapterPosition
                    val activityEntities = adapter?.getActivities()
                    semaphoreDB?.activityDao()?.deleteActivityEntity(activityEntities!!.get(position))
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f

                // Store new position in database after moving
                adapter?.mActivityEntity?.forEachIndexed { index, activity ->
                    AppExecutors.getInstance().diskIO().execute {
                        semaphoreDB?.activityDao()?.updateActivityEntityPosition(activity.id, index)
                    }
                }
            }
        }

        itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper!!.attachToRecyclerView(recyclerView)

        setupView(view)

        return view
    }

    override fun onStart() {
        super.onStart()
        prefs.isActivityFragmentForeground = true
        prefs.mSharedPreferences?.registerOnSharedPreferenceChangeListener(this) // Register prefs for change listening in this Activity
    }

    override fun onStop() {
        super.onStop()
        prefs.isActivityFragmentForeground = false
    }

    override fun onPause() {
        super.onPause()
        prefs.isActivityFragmentForeground = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        if (context is OnListFragmentInteractionListener) {
//            onListFragmentInteractionListener = context
//        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.stopAllActivities()
        prefs.isPlayingAllActivities = false
        prefs.isPlayingAllInOrderActivities = false
        prefs.mSharedPreferences?.unregisterOnSharedPreferenceChangeListener(this) // Register prefs for change listening in this Activity
    }

    fun moveItem(from: Int, to: Int) {
        val fromActivity = adapter?.mActivityEntity?.get(from)
        adapter?.mActivityEntity?.removeAt(from)
        if (to < from) {
            adapter?.mActivityEntity?.add(to, fromActivity!!)
        } else {
            adapter?.mActivityEntity?.add(to - 1, fromActivity!!)
        }
    }

    fun setupView(view: View) {

        playAllButton = view.findViewById(R.id.play_all_button_)
        playAllInOrderButton = view.findViewById(R.id.play_all_in_order_button_)
        stopAllButton = view.findViewById(R.id.stop_all_button_)

        playAllButton?.setOnClickListener {

            prefs.isPlayingAllActivities = true

            adapter?.mActivityEntity?.forEachIndexed { index, activityEntity ->
                val holder = recyclerView?.getChildViewHolder(recyclerView!!.getChildAt(index)) as ActivityRecyclerViewAdapter.ViewHolder?
                holder!!.itemView.isEnabled = false
                activityEntity.countDownTimer?.start()
            }

        }

        playAllInOrderButton!!.setOnClickListener {

            prefs.isPlayingAllInOrderActivities = true

            semaphore?.drainPermits() // Just in case
            semaphore = Semaphore(1)

            for (i in adapter?.mActivityEntity!!.indices) {
                prefs.totalActivitiesHours = prefs.totalActivitiesHours?.plus(adapter?.mActivityEntity!![i].hours!!)
                prefs.totalActivitiesMinutes = prefs.totalActivitiesMinutes?.plus(adapter?.mActivityEntity!![i].minutes!!)
                prefs.totalActivitiesSeconds = prefs.totalActivitiesSeconds?.plus(adapter?.mActivityEntity!![i].seconds!!)
                val holder = recyclerView!!.getChildViewHolder(recyclerView!!.getChildAt(i)) as ActivityRecyclerViewAdapter.ViewHolder?
                holder!!.itemView.isEnabled = false
                thread {
                    semaphore!!.acquire(1)
                    println("Aquired")
                    adapter?.play(holder, i, semaphore!!)
                }
            }

        }

        stopAllButton!!.setOnClickListener {

            if (prefs.isPlayingAllActivities) {
                prefs.isPlayingAllActivities = false
            }
            if (prefs.isPlayingAllInOrderActivities) {
                prefs.isPlayingAllInOrderActivities = false
            }
        }

        fabPlaceholder!!.setOnClickListener {
            val newActivityFragment = NewActivityDialogFragment()
            newActivityFragment.show(fragmentManager!!, "newActivity")
        }

    }

    fun retrieveActivityEntities() {
        semaphoreDB?.activityDao()?.getActivityEntities()?.observe(this, Observer<List<ActivityEntity>> { activityentity ->
            //adapter?.mActivityEntity = activityentity as ArrayList<ActivityEntity>?
            adapter?.setActivities(activityentity as ArrayList<ActivityEntity>)
        })
    }

    // TODO: Need to do better
    fun showPlayButtons() {
        for (i in 0 until recyclerView!!.childCount) {
            val playButton = recyclerView?.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<FloatingActionButton>(R.id.single_play_button_)
            val options = recyclerView?.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<TextView>(R.id.activity_options_)
            playButton.show()
            options.visibility = View.VISIBLE
        }
    }

    fun hidePlayButtons() {
        for (i in 0 until recyclerView!!.childCount) {
            val playButton = recyclerView?.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<FloatingActionButton>(R.id.single_play_button_)
            val options = recyclerView?.findViewHolderForAdapterPosition(i)!!.itemView.findViewById<TextView>(R.id.activity_options_)
            playButton.hide()
            options.visibility = View.GONE
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            IS_PLAYING_ALL_IN_ORDER_ACTIVITIES -> {
                if (prefs.isPlayingAllInOrderActivities) {
                    // Disable swiping while playing
                    itemTouchHelper!!.attachToRecyclerView(null)

                    // Update Play All and Stop All buttons
                    stopAllButton!!.visibility = View.VISIBLE
                    playAllButton!!.visibility = View.GONE
                    playAllInOrderButton!!.visibility = View.GONE

                    //hidePlayButtons()
                } else {
                    // Update Play All and Stop All buttons
                    stopAllButton!!.visibility = View.GONE
                    playAllButton!!.visibility = View.VISIBLE
                    playAllInOrderButton!!.visibility = View.VISIBLE

                    // Enable swiping while playing
                    itemTouchHelper!!.attachToRecyclerView(recyclerView)

                    //showPlayButtons()

                    semaphore!!.drainPermits()

                    adapter?.stopAllActivities()
                }
            }
            IS_PLAYING_ALL_ACTIVITIES -> {
                if (prefs.isPlayingAllActivities) {
                    // Disable swiping while playing
                    itemTouchHelper!!.attachToRecyclerView(null)

                    //hidePlayButtons()

                    // Update Play All and Stop All buttons
                    stopAllButton!!.visibility = View.VISIBLE
                    playAllButton!!.visibility = View.GONE
                    playAllInOrderButton!!.visibility = View.GONE
                } else {
                    // Update Play All and Stop All buttons
                    stopAllButton!!.visibility = View.GONE
                    playAllButton!!.visibility = View.VISIBLE
                    playAllInOrderButton!!.visibility = View.VISIBLE

                    // Enable swiping while playing
                    itemTouchHelper!!.attachToRecyclerView(recyclerView)

                    //showPlayButtons()

                    adapter?.stopAllActivities()
                }
            }
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ActivityFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
