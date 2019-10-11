package xyz.cortland.fittimer.android.adapter

import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.tapadoo.alerter.Alerter
import io.karn.notify.Notify
import kotlinx.android.synthetic.main.activity_list_content.view.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.activities.ActivityDetailActivity
import xyz.cortland.fittimer.android.activities.ActivityListActivity
import xyz.cortland.fittimer.android.custom.CountDownTimer
import xyz.cortland.fittimer.android.database.ActivityDatabase
import xyz.cortland.fittimer.android.extensions.dbHandler
import xyz.cortland.fittimer.android.extensions.hideTimerNotification
import xyz.cortland.fittimer.android.extensions.showTimerNotification
import xyz.cortland.fittimer.android.extensions.speakText
import xyz.cortland.fittimer.android.fragments.NewActivityDialogFragment
import xyz.cortland.fittimer.android.helpers.CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION
import xyz.cortland.fittimer.android.model.ActivityModel
import xyz.cortland.fittimer.android.receivers.ActivityFinishedReceiver
import xyz.cortland.fittimer.android.helpers.CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING
import xyz.cortland.fittimer.android.helpers.prefs
import java.io.File
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


class ActivityAdapter(var parentActivity: ActivityListActivity?, var mActivityModels: List<ActivityModel>) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    var activityId: Int? = null

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as ActivityModel
            activityId = v.id
            val intent = Intent(v.context, ActivityDetailActivity::class.java).apply {
                putExtra("arg_parcel_activity", item)
                putExtra("arg_activity_id", activityId!!)
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(parentActivity!!, v, "viewActivity")
            v.context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activityModel = mActivityModels[position]

        val cursor = parentActivity!!.dbHandler.getAllActivities()
        if (!cursor!!.moveToPosition(position)) {
            return
        }

        var hours = activityModel.hours
        var minutes = activityModel.minutes
        var seconds = activityModel.seconds

        val totalTimeInMillis = hours?.times(3600000)!! + minutes?.times(60000)!! + seconds?.times(1000)!!

        if (activityModel.isDefaultState!!) {
            holder.stopButton.show()
            holder.playButton.show()
            holder.activityControls.visibility = View.GONE
            holder.activityOptionsButton.visibility = View.VISIBLE
            holder.resumeButton.hide()
            holder.pauseButton.show()
            holder.activityImage.visibility = View.VISIBLE
            holder.itemView.isEnabled = true

            holder.activityView.text = activityModel.activityName
            holder.hoursView.text = if (activityModel.hours in 0..9) "0${activityModel.hours.toString()}" else activityModel.hours.toString()
            holder.minutesView.text = if (activityModel.minutes in 0..9) "0${activityModel.minutes.toString()}" else activityModel.minutes.toString()
            holder.secondsView.text = if (activityModel.seconds in 0..9) "0${activityModel.seconds.toString()}" else activityModel.seconds.toString()
            if (activityModel.activityImage != null) {
                Glide.with(parentActivity!!).load(File(activityModel.activityImage)).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.activityImage)
            } else {
                holder.activityImage.visibility = View.GONE
            }

            // Mostly for Play All
            holder.activityProgressBar.progressMax = (totalTimeInMillis / 1000).toFloat()
            holder.activityProgressBar.progress = (totalTimeInMillis / 1000).toFloat()

        } else {
            return
        }

        activityModel.countDownTimer = object: CountDownTimer(totalTimeInMillis.toLong(), 1000) {

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
                if (parentActivity!!.isPaused!! == true) {
                    finishedActivityNotification(activityModel)
                }
                holder.playButton.show()
                holder.hoursView.text = if (activityModel.hours in 0..9) "0${activityModel.hours.toString()}" else activityModel.hours.toString()
                holder.minutesView.text = if (activityModel.minutes in 0..9) "0${activityModel.minutes.toString()}" else activityModel.minutes.toString()
                holder.secondsView.text = if (activityModel.seconds in 0..9) "0${activityModel.seconds.toString()}" else activityModel.seconds.toString()
                holder.activityControls.visibility = View.GONE

                // Set the progress ring back to default
                holder.activityProgressBar.progress = (totalTimeInMillis / 1000).toFloat()
            }

            override fun countdownStart() {

                if (activityModel.activitySpeech == 1) {
                    parentActivity!!.speakText(activityModel.activityName!!)
                }

                holder.playButton.hide()
                holder.activityOptionsButton.visibility = View.GONE

                holder.activityControls.visibility = View.VISIBLE

            }

            override fun countdownCancel() {

                activityModel.isDefaultState = true

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
            activityModel.countDownTimer!!.start()
        }
        holder.stopButton.setOnClickListener {
            activityModel.countDownTimer!!.cancel()
        }

        holder.pauseButton.setOnClickListener {
            activityModel.countDownTimer!!.pause()
        }

        holder.resumeButton.setOnClickListener {
            activityModel.countDownTimer!!.resume()
        }

        holder.activityOptionsButton.setOnClickListener {

            prefs.isOptionEditingActivity = true
            prefs.optionEditSelectedActivityId = activityModel.id!!

            activityId = activityModel.id

            // Create popup menu
            val popup = PopupMenu(parentActivity!!, holder.activityOptionsButton)

            // Inflate Activity Options Menu
            popup.inflate(R.menu.activity_options_menu)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.option_edit_activity -> {
                        val newActivityDialogFragment: NewActivityDialogFragment = NewActivityDialogFragment.newInstance(activityModel, activityId!!)
                        newActivityDialogFragment.show(parentActivity!!.supportFragmentManager, "ModifyActivity")
                    }
                    R.id.option_copy_activity -> {
                        print("")
                    }
                }
                true
            }

            popup.show()

        }

        with(holder.itemView) {
            holder.itemView.id = cursor.getInt(cursor.getColumnIndex(ActivityDatabase.COLUMN_ID))
            parentActivity!!.dbHandler.setPosition(cursor.getInt(cursor.getColumnIndex(ActivityDatabase.COLUMN_ID)), position)
            cursor.close()
            tag = activityModel
            setOnClickListener(onClickListener)
        }
    }

    fun play(mHolder: ViewHolder, position: Int, semaphore: Semaphore) {

        val activityModel = mActivityModels[position]

        var hours = activityModel.hours
        var minutes = activityModel.minutes
        var seconds = activityModel.seconds

        val totalTimeInMillis = hours?.times(3600000)!! + minutes?.times(60000)!! + seconds?.times(1000)!!

        activityModel.isDefaultState = false

        Handler(Looper.getMainLooper()).post {

            //mHolder.stopButton.hide()
            //mHolder.playButton.hide()

            mHolder.activityProgressBar.progressMax = (totalTimeInMillis / 1000).toFloat()

            activityModel.countDownTimer = object: CountDownTimer(totalTimeInMillis.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {

                    val _hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val _minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1)
                    val _seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1)

                    mHolder.hoursView.text = if (_hours in 0..9) "0$_hours" else "$_hours"
                    mHolder.minutesView.text = if (_minutes in 0..9) "0$_minutes" else "$_minutes"
                    mHolder.secondsView.text = if (_seconds in 0..9) "0$_seconds" else "$_seconds"

                    prefs.currentPlayingAllRemainingTime = millisUntilFinished

                    if (parentActivity!!.isPaused!!) {
                        //updateNotification(activityModel,"${millisUntilFinished / 1000} seconds remaining.")
                        parentActivity!!.showTimerNotification(activityModel, false)
                    }
                    mHolder.activityProgressBar.setProgressWithAnimation((millisUntilFinished / 1000).toFloat())
                }

                override fun onFinish() {
                    if (position == mActivityModels.size - 1) {
                        prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING)
                        prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION)
                        prefs.isPlayingAllInOrderActivities = false
                        if (parentActivity!!.isPaused!!) {
                            parentActivity!!.hideTimerNotification()
                            finishedActivities()
                        } else {
                            Alerter.create(parentActivity)
                                .setTitle("Activity Complete!")
                                .setDuration(500)
                                .setBackgroundColorRes(R.color.colorAccent)
                                .enableSwipeToDismiss()
                                .enableVibration(true)
                                .show()
                        }
                    } else {
                        // Necessary because countdowntimer is weird and stops doing shit at 1
                        if (parentActivity!!.isPaused!!) {
                            parentActivity!!.hideTimerNotification()
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

                    if (activityModel.activitySpeech == 1) {
                        // TODO: Need shared textToSpeech
                        parentActivity!!.speakText(activityModel.activityName!!)
                    }

                    mHolder.activityControls.visibility = View.VISIBLE
                    mHolder.activityOptionsButton.visibility = View.GONE

                    mHolder.playButton.hide()

                }

                override fun countdownCancel() {

                    when {
                        position != mActivityModels.size - 1 -> {
                            if (parentActivity!!.isPaused!!) {
                                parentActivity!!.hideTimerNotification()
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
                        position == mActivityModels.size - 1 -> {
                            prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_REMAINING)
                            prefs.removePreferences(CURRENT_PLAYING_ALL_IN_ORDER_ACTIVITY_POSITION)
                            prefs.isPlayingAllInOrderActivities = false
                            if (parentActivity!!.isPaused!!) {
                                parentActivity!!.hideTimerNotification()
                                finishedActivities()
                            } else {
                                Alerter.create(parentActivity)
                                    .setTitle("Activity Complete!")
                                    .setDuration(500)
                                    .enableSwipeToDismiss()
                                    .enableVibration(true)
                                    .setBackgroundColorRes(R.color.colorAccent)
                                    .show()
                            }
                        }
                        else -> {
                            return
                        }
                    }
                }

                override fun countdownPause() {
                    if (parentActivity!!.isPaused!!) {
                        parentActivity!!.showTimerNotification(activityModel, true)
                    }
                    mHolder.pauseButton.hide()
                    mHolder.resumeButton.show()
                }

                override fun countdownResume() {
                    if (parentActivity!!.isPaused!!) {
                        parentActivity!!.showTimerNotification(activityModel, false)
                    }
                    mHolder.resumeButton.hide()
                    mHolder.pauseButton.show()
                }

            }
            activityModel.countDownTimer!!.start()
        }
    }

    fun finishedActivityNotification(activityModel: ActivityModel) {

        val resultIntent = Intent(parentActivity!!.applicationContext, parentActivity!!::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        Notify
            .with(parentActivity!!)
            .content {
                title = "Finished"
                text = "${activityModel.activityName}\nfor ${activityModel.seconds.toString()} seconds."
            }
            .meta {
                clickIntent = PendingIntent.getActivity(parentActivity!!.applicationContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            .header {
                color = 0x706DF8 // Set the color of the bell icon
            }
            .show()
    }

    fun stopAllActivities() {
        mActivityModels.forEach {
            it.countDownTimer?.cancel()
            it.isDefaultState = true
        }
        notifyDataSetChanged()
    }

    fun finishedActivities() {
        val intent = Intent(parentActivity!!, ActivityFinishedReceiver::class.java)
        intent.action = "playingall.activityModel.finished"
        parentActivity!!.sendBroadcast(intent)
    }

    override fun getItemCount() = mActivityModels.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var secondsView: TextView = view.seconds
        var minutesView: TextView = view.minutes
        var hoursView: TextView = view.hours
        var activityView: TextView = view.activityModel
        var playButton: FloatingActionButton = view.single_play_button
        var stopButton: FloatingActionButton = view.single_stop_button
        var pauseButton: FloatingActionButton = view.single_pause_button
        var resumeButton: FloatingActionButton = view.single_resume_button
        var activityImage: ImageView = view.activity_image
        var activityControls: LinearLayout = view.activity_controls
        var activityProgressBar: CircularProgressBar = view.activity_progress_bar
        var activityOptionsButton: TextView = view.activity_options
    }

}