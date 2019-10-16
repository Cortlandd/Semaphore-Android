package xyz.cortland.semaphore.android.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_finished_activities.*
import xyz.cortland.semaphore.android.R
import xyz.cortland.semaphore.android.helpers.*
import java.util.concurrent.TimeUnit

class FinishedActivitiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finished_activities)

        val hours = intent.getIntExtra("total_hours", 0)
        val minutes = intent.getIntExtra("total_minutes", 0)
        val seconds = intent.getIntExtra("total_seconds", 0)

        val totalTime = hours.times(3600000) + minutes.times(60000) + seconds.times(1000)

        val h = TimeUnit.MILLISECONDS.toHours(totalTime.toLong())
        val m = TimeUnit.MILLISECONDS.toMinutes(totalTime.toLong()) % TimeUnit.HOURS.toMinutes(1)
        val s = TimeUnit.MILLISECONDS.toSeconds(totalTime.toLong()) % TimeUnit.MINUTES.toSeconds(1)

        activities_complete_hours.text = if (h in 0..9) "0$h HR" else "$h HR"
        activities_complete_minutes.text = if (m in 0..9) "0$m MIN" else "$m MIN"
        activities_complete_seconds.text = if (s in 0..9) "0$s SEC" else "$s SEC"

        // TODO: Show completed activities
        //val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        //activities_completed_list.adapter = adapter
        //prefs.removePreferences(ACTIVITIES_LIST)

        prefs.removePreferences(ACTIVITIES_TOTAL_HOURS)
        prefs.removePreferences(ACTIVITIES_TOTAL_MINUTES)
        prefs.removePreferences(ACTIVITIES_TOTAL_SECONDS)

        close_completed_activities_button.setOnClickListener {
            finish()
        }
    }

}
