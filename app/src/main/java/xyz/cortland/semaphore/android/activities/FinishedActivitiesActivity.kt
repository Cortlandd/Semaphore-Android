package xyz.cortland.semaphore.android.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_finished_activities.*
import xyz.cortland.semaphore.android.R
import xyz.cortland.semaphore.android.helpers.*
import java.util.concurrent.TimeUnit
import android.widget.TextView
import com.bumptech.glide.Glide
import android.content.Context.LAYOUT_INFLATER_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Context
import android.view.View
import androidx.lifecycle.Observer
import org.jetbrains.anko.doAsync
import xyz.cortland.semaphore.android.database.AppExecutors
import xyz.cortland.semaphore.android.extensions.semaphoreDB
import xyz.cortland.semaphore.android.model.ActivityEntity


class FinishedActivitiesActivity : AppCompatActivity() {

    //var activityEntities: ArrayList<ActivityEntity>? = ArrayList<ActivityEntity>()
    var activityEntities: List<ActivityEntity> = emptyList()
    var h: Long = 0
    var m: Long = 0
    var s: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finished_activities)

        AppExecutors.getInstance().diskIO().execute {
            activityEntities = semaphoreDB!!.activityDao().getActivityEntitiesList()
        }

        val hours = intent.getIntExtra("total_hours", prefs.totalActivitiesHours!!)
        val minutes = intent.getIntExtra("total_minutes", prefs.totalActivitiesMinutes!!)
        val seconds = intent.getIntExtra("total_seconds", prefs.totalActivitiesSeconds!!)

        val totalTime = hours.times(3600000) + minutes.times(60000) + seconds.times(1000)

        h = TimeUnit.MILLISECONDS.toHours(totalTime.toLong())
        m = TimeUnit.MILLISECONDS.toMinutes(totalTime.toLong()) % TimeUnit.HOURS.toMinutes(1)
        s = TimeUnit.MILLISECONDS.toSeconds(totalTime.toLong()) % TimeUnit.MINUTES.toSeconds(1)

        activities_complete_hours.text = if (h in 0..9) "0$h HR" else "$h HR"
        activities_complete_minutes.text = if (m in 0..9) "0$m MIN" else "$m MIN"
        activities_complete_seconds.text = if (s in 0..9) "0$s SEC" else "$s SEC"

        close_completed_activities_button.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        // Display Completed Activities in the UI
        val adapter = CompletedActivitiesAdapter(this, activityEntities)
        activities_completed_list.adapter = adapter

        prefs.removePreferences(ACTIVITIES_TOTAL_HOURS)
        prefs.removePreferences(ACTIVITIES_TOTAL_MINUTES)
        prefs.removePreferences(ACTIVITIES_TOTAL_SECONDS)
        AppExecutors.getInstance().diskIO().execute {
            semaphoreDB!!.activityDao().getActivityEntitiesList().forEach {
                it.activityEntityState = "Not Started"
            }
        }
    }

}

class CompletedActivitiesAdapter(private val context: Context, activityEntities: List<ActivityEntity>) : BaseAdapter() {

    private val activityEntities: List<ActivityEntity>

    init {
        this.activityEntities = activityEntities
    }

    override fun getCount(): Int {
        return activityEntities.size
    }

    override fun getItem(position: Int): Any {
        return activityEntities[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = layoutInflater.inflate(R.layout.completed_activities_list_layout, null)

        val completedActivityName = view.findViewById<TextView>(R.id.completed_activity_name)
        val completedActivityState = view.findViewById<TextView>(R.id.completed_activity_state)

        completedActivityName.text = activityEntities[position].activityName
        completedActivityState.text = activityEntities[position].activityEntityState

        return view
    }

}