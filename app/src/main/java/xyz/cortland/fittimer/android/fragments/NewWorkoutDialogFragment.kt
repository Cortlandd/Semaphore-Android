package xyz.cortland.fittimer.android.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.model.Workout
import xyz.klinker.giphy.GiphyView
import java.lang.ClassCastException
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.add_workout.*
import xyz.cortland.fittimer.android.FitTimer
import xyz.cortland.fittimer.android.database.WorkoutDatabase
import xyz.cortland.fittimer.android.extensions.speakText
import xyz.cortland.fittimer.android.helpers.IMAGE_PICK_CODE
import xyz.cortland.fittimer.android.utils.GlobalPreferences
import xyz.cortland.fittimer.android.utils.ImageFilePath
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


// TODO: (MAYBE) Implemented an EditWorkoutDialogFragment Dialog Class
class NewWorkoutDialogFragment: DialogFragment() {

    var gifImageLocation: String? = null
    var galleryImageLocation: String? = null
    var imagePath: String? = null
    var mGlobalPreferences: GlobalPreferences? = null

    var workoutImage: ImageView? = null
    var workoutImagePlaceholder: ImageView? = null
    var hoursNumberPicker: NumberPicker? = null
    var minutesNumberPicker: NumberPicker? = null
    var secondsNumberPicker: NumberPicker? = null
    var workoutSpeech: SwitchCompat? = null
    var searchGiphyLayout: LinearLayout? = null
    var giphyView: GiphyView? = null
    var workoutText: EditText? = null
    var fullControls: NestedScrollView? = null

    var workoutValue: String? = ""
    var hoursValue: Int? = null
    var minutesValue: Int? = null
    var secondsValue: Int? = null
    var workoutImageValue: String? = null
    var workoutSpeechValue: Int? = 1

    var workout: Workout? = null
    var workoutId: Int? = null

    var positiveButton: Button? = null
    var negativeButton: Button? = null

    interface NewWorkoutDialogListener {
        fun onSaveClick(dialog: DialogFragment, workout: Workout)
        fun onCancelClick(dialog: DialogFragment)
    }

    var newWorkoutDialogListener: NewWorkoutDialogListener? = null

    companion object {
        fun newInstance(workout: Workout, id: Int): NewWorkoutDialogFragment {
            val newWorkoutDialogFragment = NewWorkoutDialogFragment()
            val args = Bundle()
            args.putParcelable("arg_workout", workout)
            args.putInt("arg_workout_id", id)
            newWorkoutDialogFragment.arguments = args
            return newWorkoutDialogFragment
        }
    }

    override fun onStart() {
        super.onStart()
        // Because my goofass
        val d: AlertDialog = dialog as AlertDialog
        if (d != null) {
            positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = d.getButton(Dialog.BUTTON_NEGATIVE)
            if (workoutText!!.text.toString().length >= 2) {
                positiveButton?.isEnabled = true
            } else {
                positiveButton?.isEnabled = false
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val builder = AlertDialog.Builder(this.activity!!, R.style.AlertDialogTheme)
        mGlobalPreferences = FitTimer.applicationContext().preferences

        val dialogView = activity?.layoutInflater?.inflate(R.layout.add_workout, null)

        workout = arguments?.getParcelable("arg_workout")
        workoutId = arguments?.getInt("arg_workout_id")

        setupView(dialogView!!)


        if (workout == null) {
            builder.setTitle(R.string.add_workout)
        } else {
            builder.setTitle(R.string.edit_workout)
            hoursNumberPicker?.value = workout?.hours!!
            minutesNumberPicker?.value = workout?.minutes!!
            secondsNumberPicker?.value = workout?.seconds!!
            workoutText?.setText(workout?.workoutName)
            if (workout!!.workoutSpeech == 1) {
                workoutSpeechValue = 1
                workoutSpeech!!.isChecked = true
            } else {
                workoutSpeechValue = 0
                workoutSpeech!!.isChecked = false
            }

            if (workout?.workoutImage != null) {
                workoutImage!!.visibility = View.VISIBLE
                workoutImagePlaceholder!!.visibility = View.GONE
                Glide.with(this).load(Uri.fromFile(File(workout?.workoutImage))).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.circular_progress_bar).into(workoutImage!!)
                workoutImageValue = workout?.workoutImage
            } else {
                workoutImage!!.visibility = View.GONE
                workoutImagePlaceholder!!.visibility = View.VISIBLE
            }
        }

        builder.setView(dialogView)
            .setPositiveButton(R.string.save) { dialog, id ->
                mGlobalPreferences!!.workoutModified = true
                validateImagePath()
                newWorkoutDialogListener?.onSaveClick(this, Workout(hours = hoursValue, minutes = minutesValue, seconds = secondsValue, workoutName = workoutValue, workoutImage = workoutImageValue, workoutSpeech = workoutSpeechValue))
            }
            .setNegativeButton(R.string.close) { dialog, id ->
                // TODO: If cancel is selected and image isn't null, delete anything created
                newWorkoutDialogListener?.onCancelClick(this)
            }


        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            newWorkoutDialogListener = activity as NewWorkoutDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement NewWorkoutDialogListener")
        }
    }

    fun setupView(dialogView: View) {

        fullControls = dialogView.findViewById(R.id.workout_content_view)
        workoutText = dialogView.findViewById(R.id.workout_text)
        giphyView = dialogView.findViewById(R.id.search_giphy_view)
        val closeGiphyButton = dialogView.findViewById<Button>(R.id.close_giphysearch_button)
        searchGiphyLayout = dialogView.findViewById(R.id.gif_search_view)
        workoutSpeech = dialogView.findViewById(R.id.workout_to_speech)
        hoursNumberPicker = dialogView.findViewById(R.id.hours_number_picker)
        minutesNumberPicker = dialogView.findViewById(R.id.minutes_number_picker)
        secondsNumberPicker = dialogView.findViewById(R.id.seconds_number_picker)
        workoutImage = dialogView.findViewById<ImageView>(R.id.selected_image)
        workoutImagePlaceholder = dialogView.findViewById(R.id.selected_image_placeholder)

        // TODO: Get API Key from Giphy
        // TODO: Create separate Alert Dialog for this with a callback
        giphyView?.setSelectedCallback {

            // Make images visible
            workoutImage!!.visibility = View.VISIBLE
            workoutImagePlaceholder!!.visibility = View.GONE

            gifImageLocation = it.path?.toString()
            Glide.with(this).load(it).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.circular_progress_bar).into(workoutImage!!)
            galleryImageLocation = null
            imagePath = null
            searchGiphyLayout?.visibility = View.GONE
            showControlButtons()
        }


        workoutText!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                workoutValue = s.toString()
                positiveButton?.isEnabled = s.toString().length >= 2
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                workoutValue = s.toString()
                positiveButton?.isEnabled = s.toString().length >= 2
            }

        })

        closeGiphyButton.setOnClickListener {
            searchGiphyLayout?.visibility = View.GONE
            giphyView?.invalidate()
            showControlButtons()
        }

        workoutSpeech!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                workoutSpeechValue = 1
                if (workoutText?.text.toString() != "") {
                    context?.speakText(workoutText?.text.toString())
                }
            } else {
                workoutSpeechValue = 0
            }
        }

        hoursValue = hoursNumberPicker!!.value
        hoursNumberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
            hoursValue = newVal
        }

        minutesValue = minutesNumberPicker!!.value
        minutesNumberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
            minutesValue = newVal
        }

        secondsValue = secondsNumberPicker!!.value
        secondsNumberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
            secondsValue = newVal
        }

        workoutImage?.setOnClickListener {
            tapWorkoutImage()
        }
        workoutImagePlaceholder?.setOnClickListener {
            tapPlaceholderImage()
        }

    }

    private fun validateImagePath() {
        when {
            // For New Workouts
            imagePath != null && gifImageLocation == null -> {
                saveSelectedImage(imagePath!!)
                workoutImageValue = galleryImageLocation
            }
            gifImageLocation != null && imagePath == null -> {
                workoutImageValue = gifImageLocation
            }
            // For Existing Workouts
            imagePath != null && workoutImageValue != workout?.workoutImage -> {
                saveSelectedImage(imagePath!!)
                workoutImageValue = galleryImageLocation
            }
            gifImageLocation != null  && workoutImageValue != workout?.workoutImage -> {
                workoutImageValue = gifImageLocation
            }
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun saveSelectedImage(path: String): Uri {

        // Generate random string
        val randomName = java.util.UUID.randomUUID().toString()

        val extension = path.substring(path.lastIndexOf(".")) // .jpg, .png, etc

        val saveLocation = this.activity!!.cacheDir.absolutePath

        val dir: File = File(saveLocation)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val saveImage: File = File(saveLocation, "$randomName$extension")
        if (!saveImage.createNewFile()) {
            // File exists, return
            return Uri.fromFile(saveImage)
        } else {
            val imageDownload: File = File(path)
            val inStream = FileInputStream(imageDownload)
            val outStream = FileOutputStream(saveImage)
            val inChannel = inStream.channel
            val outChannel = outStream.channel
            inChannel.transferTo(0, inChannel.size(), outChannel)
            inStream.close()
            outStream.close()
        }

        galleryImageLocation = ImageFilePath.getPath(this.activity!!, Uri.fromFile(saveImage))

        return Uri.fromFile(saveImage)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {

                    val uri = data.data

                    if (imagePath != null) {
                        val deletedFile = File(imagePath)
                        deletedFile.delete()
                    }

                    imagePath = ImageFilePath.getPath(this.activity!!, uri)

                    workoutImage!!.visibility = View.VISIBLE
                    workoutImagePlaceholder!!.visibility = View.GONE

                    try {
                        Glide.with(this).load(uri).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.circular_progress_bar).into(workoutImage!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    // Set gif image null to avoid issues
                    gifImageLocation = null

                }
            }
        }
    }

    fun tapWorkoutImage() {
        val builder = AlertDialog.Builder(this.activity!!)
        if (workout == null) {
            builder.setTitle(R.string.add_workout_image)
        } else {
            builder.setTitle(R.string.edit_workout_image)
        }
        val options = arrayOf<String>(getString(R.string.upload_image), getString(R.string.search_gif), getString(R.string.remove_current_image))
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> { // Gallery Image
                    if (ActivityCompat.checkSelfPermission(this.activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this.activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), IMAGE_PICK_CODE)
                    } else {
                        pickImageFromGallery()
                    }
                }
                1 -> { // Search GIF
                    giphyView?.initializeView("dc6zaTOxFJmzC", 5 * 1024 * 1024)
                    searchGiphyLayout?.visibility = View.VISIBLE
                    hideControlButtons()
                }
                2 -> { // Remove Current Image
                    if (workout != null) {
                        // Delete File associated from cache
                        if (workout?.workoutImage != null) {
                            File(workout?.workoutImage).delete()
                        }
                        Glide.with(this.activity!!).clear(workoutImage!!)
                        // Glide clearing Imageview
                        // Update in the database
                        WorkoutDatabase(this.activity!!, null).updateWorkoutImage(workoutId!!)
                        // Update workout image views
                        workoutImagePlaceholder!!.visibility = View.VISIBLE
                        workoutImage!!.visibility = View.GONE
                        // Preferences to indicate image removed
                        mGlobalPreferences!!.currentImageRemoved = true
                    } else {

                        if (gifImageLocation != null) {

                            val gif = File(gifImageLocation)
                            if (gif.exists()) {
                                gif.delete()
                                Glide.with(this.activity!!).clear(workoutImage!!)
                            }

                            workoutImage?.visibility = View.GONE
                            workoutImagePlaceholder?.visibility = View.VISIBLE
                        }

                        if (imagePath != null) {
                            val img = File(imagePath)
                            if (img.exists()) {
                                img.delete()
                                Glide.with(this.activity!!).clear(workoutImage!!)
                            }

                            workoutImage?.visibility = View.GONE
                            workoutImagePlaceholder?.visibility = View.VISIBLE
                        }

                    }

                }
            }
        }.create().show()
    }

    fun tapPlaceholderImage() {
        val builder = AlertDialog.Builder(this.activity!!)
        if (workout?.workoutImage == null) {
            builder.setTitle(R.string.add_workout_image)
        } else {
            builder.setTitle(R.string.edit_workout_image)
        }
        val options = arrayOf<String>(getString(R.string.upload_image), getString(R.string.search_gif))
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> { // Gallery Image
                    if (ActivityCompat.checkSelfPermission(this.activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this.activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), IMAGE_PICK_CODE)
                    } else {
                        pickImageFromGallery()
                    }
                }
                1 -> { // Search GIF
                    giphyView?.initializeView("dc6zaTOxFJmzC", 5 * 1024 * 1024)
                    searchGiphyLayout?.visibility = View.VISIBLE
                    hideControlButtons()
                }
            }
        }.create().show()
    }

    fun hideControlButtons() {
        fullControls?.visibility = View.GONE
        negativeButton?.visibility = View.GONE
        positiveButton?.visibility = View.GONE
    }

    fun showControlButtons() {
        fullControls?.visibility = View.VISIBLE
        negativeButton?.visibility = View.VISIBLE
        positiveButton?.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}