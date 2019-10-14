package xyz.cortland.semaphore.android.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.android.synthetic.main.activity_activity_detail.*
import xyz.cortland.semaphore.android.R
import xyz.cortland.semaphore.android.activities.ActivityDetailActivity
import xyz.cortland.semaphore.android.model.ActivityModel
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
    var hoursView: TextView? = null
    var minutesView: TextView? = null
    var secondsView: TextView? = null
    var progress: CircularProgressBar? = null
    var totalTimeInMillis: Int = 0

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

        hoursView = rootView.findViewById(R.id.detail_hours)
        minutesView = rootView.findViewById(R.id.detail_minutes)
        secondsView = rootView.findViewById(R.id.detail_seconds)
        progress = rootView.findViewById(R.id.detail_progress_bar)

        // Show the ActivityModel content as text in a TextView.
        item.let {

            var hours = it?.hours
            var minutes = it?.minutes
            var seconds = it?.seconds

            totalTimeInMillis = hours?.times(3600000)!! + minutes?.times(60000)!! + seconds?.times(1000)!!

            hoursView?.text = if (it?.hours in 0..9) "0${it?.hours} hr" else "${it?.hours} hr" //it?.hours.toString()
            minutesView?.text = if (it?.minutes in 0..9) "0${it?.minutes} min" else "${it?.minutes} min"
            secondsView?.text = if (it?.seconds in 0..9) "0${it?.seconds} sec" else "${it?.seconds} sec"
            progress?.progressMax = (totalTimeInMillis / 1000).toFloat()
            progress?.progress = (totalTimeInMillis / 1000).toFloat()
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
