package xyz.cortland.semaphore.android.adapter

import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import io.karn.notify.Notify
import xyz.cortland.semaphore.android.R

import kotlinx.android.synthetic.main.fragment_activity.view.*
import org.jetbrains.anko.doAsync
import xyz.cortland.semaphore.android.activities.ActivityListActivity
import xyz.cortland.semaphore.android.custom.CountDownTimer
import xyz.cortland.semaphore.android.database.AppDatabase
import xyz.cortland.semaphore.android.database.AppExecutors
import xyz.cortland.semaphore.android.extensions.*
import xyz.cortland.semaphore.android.fragments.ActivityFragment
import xyz.cortland.semaphore.android.fragments.NewActivityDialogFragment
import xyz.cortland.semaphore.android.helpers.CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION
import xyz.cortland.semaphore.android.helpers.CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING
import xyz.cortland.semaphore.android.helpers.prefs
import xyz.cortland.semaphore.android.model.ActivityEntity
import xyz.cortland.semaphore.android.receivers.ActivityFinishedReceiver
import java.io.File
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * [RecyclerView.Adapter] that can display a [ActivityEntity] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 *
 */
class ActivityRecyclerViewAdapter(
    private val context: Context
) : RecyclerView.Adapter<ActivityRecyclerViewAdapter.ViewHolder>() {

    var activityId: Int? = null
    var mActivityEntity: ArrayList<ActivityEntity>? = null

    init {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activityEntity = mActivityEntity!!.get(position)

        val hours = activityEntity.hours
        val minutes = activityEntity.minutes
        val seconds = activityEntity.seconds

        val totalTimeInMillis = hours?.times(3600000)!! + minutes?.times(60000)!! + seconds?.times(1000)!!

        if (activityEntity.isDefaultState!!) {
            holder.stopButton.show()
            holder.playButton.show()
            holder.activityControls.visibility = View.GONE
            holder.activityOptionsButton.visibility = View.VISIBLE
            holder.resumeButton.hide()
            holder.pauseButton.show()
            holder.activityImage.visibility = View.VISIBLE
            holder.itemView.isEnabled = true

            holder.activityView.text = activityEntity.activityName
            holder.hoursView.text = if (hours in 0..9) "0$hours" else hours.toString()
            holder.minutesView.text = if (minutes in 0..9) "0$minutes" else minutes.toString()
            holder.secondsView.text = if (seconds in 0..9) "0$seconds" else seconds.toString()
            if (activityEntity.activityImage != null) {
                Glide.with(context).load(File(activityEntity.activityImage)).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.activityImage)
            } else {
                holder.activityImage.visibility = View.GONE
            }

            // Mostly for Play All
            holder.activityProgressBar.progressMax = (totalTimeInMillis / 1000).toFloat()
            holder.activityProgressBar.progress = (totalTimeInMillis / 1000).toFloat()

        } else {
            // TODO: Investigate...
            return
        }

        activityEntity.countDownTimer = object: CountDownTimer(totalTimeInMillis.toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val _hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val _minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1)
                val _seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1)

                holder.hoursView.text = if (_hours in 0..9) "0$_hours" else "$_hours"
                holder.minutesView.text = if (_minutes in 0..9) "0$_minutes" else "$_minutes"
                holder.secondsView.text = if (_seconds in 0..9) "0$_seconds" else "$_seconds"

                holder.activityProgressBar.setProgressWithAnimation((millisUntilFinished / 1000).toFloat())
            }

            override fun onFinish() {
                if (!prefs.isActivityFragmentForeground) {
                    //finishedActivityNotification(activityEntity)
                }
                holder.playButton.show()
                holder.hoursView.text = if (hours in 0..9) "0${activityEntity.hours.toString()}" else activityEntity.hours.toString()
                holder.minutesView.text = if (minutes in 0..9) "0${activityEntity.minutes.toString()}" else activityEntity.minutes.toString()
                holder.secondsView.text = if (seconds in 0..9) "0${activityEntity.seconds.toString()}" else activityEntity.seconds.toString()
                holder.activityControls.visibility = View.GONE

                // Set the progress ring back to default
                holder.activityProgressBar.progress = (totalTimeInMillis / 1000).toFloat()
            }

            override fun countdownStart() {

                if (activityEntity.activitySpeech == 1) {
                    context.speakText(activityEntity.activityName!!)
                }

                holder.playButton.hide()
                holder.activityOptionsButton.visibility = View.GONE

                holder.activityControls.visibility = View.VISIBLE

            }

            override fun countdownCancel() {

                activityEntity.isDefaultState = true

                notifyItemChanged(position)
            }

            override fun countdownPause() {
                holder.pauseButton.hide()
                holder.resumeButton.show()
            }

            override fun countdownResume() {
                holder.resumeButton.hide()
                holder.pauseButton.show()
            }

        }

        holder.playButton.setOnClickListener {
            activityEntity.countDownTimer!!.start()
        }
        holder.stopButton.setOnClickListener {
            activityEntity.countDownTimer!!.cancel()
        }

        holder.pauseButton.setOnClickListener {
            activityEntity.countDownTimer!!.pause()
        }

        holder.resumeButton.setOnClickListener {
            activityEntity.countDownTimer!!.resume()
        }

        holder.activityOptionsButton.setOnClickListener {

            prefs.isOptionEditingActivity = true
            prefs.optionEditSelectedActivityId = activityEntity.id

            activityId = activityEntity.id

            // Create popup menu
            val popup = PopupMenu(context, holder.activityOptionsButton)

            // Inflate Activity Options Menu
            popup.inflate(R.menu.activity_options_menu)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.option_edit_activity -> {
                        // Weird hack
                        val c = context as ActivityListActivity
                        val newActivityDialogFragment: NewActivityDialogFragment = NewActivityDialogFragment.newInstance(activityId!!)
                        newActivityDialogFragment.show(c.supportFragmentManager, "ModifyActivity")
                    }
                    R.id.option_copy_activity -> {
                        AppExecutors.getInstance().diskIO().execute {
                            // TODO: Issue with unique id
                            holder.semaphoreDB?.activityDao()?.insertActivityEntity(activityEntity)
                        }
                    }
                }
                true
            }

            popup.show()

        }

        with(holder.itemView) {
            holder.itemView.id = activityEntity.id
            tag = activityEntity
            // TODO: Fix. Figure out how to capture the position
//            doAsync {
//                context.semaphoreDB?.activityDao()?.updateActivityEntity(position = position, activityEntityId = activityEntity.id)
//            }
        }
    }

    fun setActivities(activityEntities: ArrayList<ActivityEntity>) {
        mActivityEntity = activityEntities
        notifyDataSetChanged()
    }

    fun getActivities(): ArrayList<ActivityEntity>? {
        return mActivityEntity
    }

    fun play(mHolder: ActivityRecyclerViewAdapter.ViewHolder, position: Int, semaphore: Semaphore) {

        val activityEntity = mActivityEntity!!.get(position)

        var hours = activityEntity.hours
        var minutes = activityEntity.minutes
        var seconds = activityEntity.seconds

        val totalTimeInMillis = hours?.times(3600000)!! + minutes?.times(60000)!! + seconds?.times(1000)!!

        activityEntity.isDefaultState = false

        Handler(Looper.getMainLooper()).post {

            //mHolder.stopButton.hide()
            //mHolder.playButton.hide()

            mHolder.activityProgressBar.progressMax = (totalTimeInMillis / 1000).toFloat()

            activityEntity.countDownTimer = object: CountDownTimer(totalTimeInMillis.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {

                    val _hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val _minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1)
                    val _seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1)

                    mHolder.hoursView.text = if (_hours in 0..9) "0$_hours" else "$_hours"
                    mHolder.minutesView.text = if (_minutes in 0..9) "0$_minutes" else "$_minutes"
                    mHolder.secondsView.text = if (_seconds in 0..9) "0$_seconds" else "$_seconds"

                    prefs.currentPlayingAllRemainingTime = millisUntilFinished

                    if (!prefs.isActivityFragmentForeground) {
                        context.showTimerNotification(activityEntity, false)
                    }
                    mHolder.activityProgressBar.setProgressWithAnimation((millisUntilFinished / 1000).toFloat())
                }

                override fun onFinish() {
                    if (position == mActivityEntity!!.size - 1) {
                        prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING)
                        prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION)
                        prefs.isPlayingAllInOrderActivities = false
                        if (!prefs.isActivityFragmentForeground) {
                            context.hideTimerNotification()
                            finishedActivities()
                        } else {
                            context.showAlert("Activities Complete", "Your Activities have been completed.", DialogInterface.OnClickListener { dialog, _ ->
                                dialog.dismiss()
                            }, hasNegativeButton = false)
                        }
                    } else {
                        // Necessary because countdowntimer is weird and stops doing shit at 1
                        if (!prefs.isActivityFragmentForeground) {
                            context.hideTimerNotification()
                        }
                        mHolder.hoursView.text = "00"
                        mHolder.minutesView.text = "00"
                        mHolder.secondsView.text = "00"
                        mHolder.activityProgressBar.progress = 0f
                        prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING)
                        prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION)
                        semaphore.release()
                    }
                }

                override fun countdownStart() {

                    prefs.currentPlayingAllActivityPosition = position

                    if (activityEntity.activitySpeech == 1) {
                        // TODO: Need shared textToSpeech
                        context.speakText(activityEntity.activityName!!)
                    }

                    mHolder.activityControls.visibility = View.VISIBLE
                    mHolder.activityOptionsButton.visibility = View.GONE

                    mHolder.playButton.hide()

                }

                override fun countdownCancel() {

                    when {
                        position != mActivityEntity?.size?.minus(1) -> {
                            if (!prefs.isActivityFragmentForeground) {
                                context.hideTimerNotification()
                            }
                            mHolder.hoursView.text = "00"
                            mHolder.minutesView.text = "00"
                            mHolder.secondsView.text = "00"
                            mHolder.activityProgressBar.progress = 0f
                            prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING)
                            prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION)
                            //mHolder.activityProgressBar.progress = 0
                            if (prefs.isPlayingAllInOrderActivities) {
                                semaphore.release()
                            } else {
                                return
                            }
                        }
                        position == mActivityEntity?.size?.minus(1) -> {
                            prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING)
                            prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION)
                            prefs.isPlayingAllInOrderActivities = false
                            if (!prefs.isActivityFragmentForeground) {
                                context.hideTimerNotification()
                                finishedActivities()
                            } else {
                                context.showAlert("Activities Complete", "Your Activities have been completed.", DialogInterface.OnClickListener { dialog, _ ->
                                    dialog.dismiss()
                                }, hasNegativeButton = false)
                            }
                        }
                        else -> {
                            return
                        }
                    }
                }

                override fun countdownPause() {
                    if (!prefs.isActivityFragmentForeground) {
                        context.showTimerNotification(activityEntity, true)
                    }
                    mHolder.pauseButton.hide()
                    mHolder.resumeButton.show()
                }

                override fun countdownResume() {
                    if (!prefs.isActivityFragmentForeground) {
                        context.showTimerNotification(activityEntity, false)
                    }
                    mHolder.resumeButton.hide()
                    mHolder.pauseButton.show()
                }

            }
            activityEntity.countDownTimer!!.start()
        }
    }

    fun finishedActivityNotification(activityEntity: ActivityEntity) {

        val resultIntent = Intent(context.applicationContext, context::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        Notify
            .with(context)
            .content {
                title = "Finished"
                text = "${activityEntity.activityName}\nfor ${activityEntity.seconds.toString()} seconds."
            }
            .meta {
                clickIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            .header {
                color = 0x706DF8 // Set the color of the bell icon
            }
            .show()
    }

    fun stopAllActivities() {
        mActivityEntity?.forEach {
            it.countDownTimer?.cancel()
            it.isDefaultState = true
        }
        notifyDataSetChanged()
    }

    fun finishedActivities() {
        val intent = Intent(context, ActivityFinishedReceiver::class.java)
        intent.action = "playingall.activityEntity.finished"
        context.sendBroadcast(intent)
    }

    override fun getItemCount(): Int {
        if (mActivityEntity?.size == null) {
            return 0
        }
        return mActivityEntity!!.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var semaphoreDB = AppDatabase.getAppDataBase(context.applicationContext)
        var secondsView: TextView = view.seconds_
        var minutesView: TextView = view.minutes_
        var hoursView: TextView = view.hours_
        var activityView: TextView = view.activityEntityName_
        var playButton: FloatingActionButton = view.single_play_button_
        var stopButton: FloatingActionButton = view.single_stop_button_
        var pauseButton: FloatingActionButton = view.single_pause_button_
        var resumeButton: FloatingActionButton = view.single_resume_button_
        var activityImage: ImageView = view.activity_image_
        var activityControls: LinearLayout = view.activity_controls_
        var activityProgressBar: CircularProgressBar = view.activity_progress_bar_
        var activityOptionsButton: TextView = view.activity_options_

    }
}
