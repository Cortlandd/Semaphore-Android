package xyz.cortland.semaphore.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import xyz.cortland.semaphore.android.activities.FinishedActivitiesActivity
import xyz.cortland.semaphore.android.helpers.prefs

class ActivityFinishedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val i = Intent(context, FinishedActivitiesActivity::class.java)
        i.putExtra("total_hours", prefs.totalActivitiesHours)
        i.putExtra("total_minutes", prefs.totalActivitiesMinutes)
        i.putExtra("total_seconds", prefs.totalActivitiesSeconds)
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }
}
