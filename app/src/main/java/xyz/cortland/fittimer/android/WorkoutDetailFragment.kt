package xyz.cortland.fittimer.android

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_workout_detail.*
import kotlinx.android.synthetic.main.workout_detail.view.*
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.model.WorkoutModel

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [WorkoutListActivity]
 * in two-pane mode (on tablets) or a [WorkoutDetailActivity]
 * on handsets.
 */
class WorkoutDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: WorkoutModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = it.getParcelable(ARG_ITEM_ID)
                activity?.toolbar_layout?.title = item?.workoutName
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.workout_detail, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.item_detail_seconds.text = it.seconds.toString()
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "workout_id"
    }
}
