package xyz.cortland.fittimer.android.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_activity_detail.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.activities.ActivityDetailActivity
import xyz.cortland.fittimer.android.model.ActivityModel
import java.io.File

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ActivityListActivity]
 * in two-pane mode (on tablets) or a [ActivityDetailActivity]
 * on handsets.
 */
class ActivityDetailFragment : Fragment() {

    /**
     * The ActivityModel Content this fragment is presenting.
     */
    private var item: ActivityModel? = null
    private var activityModelId: Int? = null
    var secondsView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = it.getParcelable(ARG_ITEM_ID)
                activityModelId = it.getInt(ARG_ACTIVITY_ID)
                activity?.toolbar_layout?.title = item?.activityName
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_detail, container, false)

        secondsView = rootView.findViewById(R.id.item_detail_seconds)

        // Show the ActivityModel content as text in a TextView.
        item.let {
            secondsView?.text = it?.seconds.toString()
            if (it?.activityImage != null) {
                Glide.with(activity!!).load(File(it.activityImage)).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(activity!!.activity_image_detail)
            }

        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "activityModel"
        const val ARG_ACTIVITY_ID = "activity_id"
    }
}
