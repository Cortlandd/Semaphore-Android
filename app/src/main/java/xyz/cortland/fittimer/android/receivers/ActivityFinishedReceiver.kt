package xyz.cortland.fittimer.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ActivityFinishedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val action = intent.action
        when (action) {
            "playingall.activityModel.finished" -> {
                val i = context.packageManager.getLaunchIntentForPackage(context.packageName) // Get reference to the app itself
                i!!.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                context.startActivity(i)
            }
        }
    }
}
