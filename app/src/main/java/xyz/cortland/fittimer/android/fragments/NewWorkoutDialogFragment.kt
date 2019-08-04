package xyz.cortland.fittimer.android.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
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
import xyz.cortland.fittimer.android.model.WorkoutModel
import xyz.klinker.giphy.GiphyView
import java.lang.ClassCastException
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.shawnlin.numberpicker.NumberPicker
import xyz.cortland.fittimer.android.utils.GlobalPreferences
import xyz.cortland.fittimer.android.utils.ImageFilePath
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


// TODO: Implemented an EditWorkoutDialogFragment Dialog Class
class NewWorkoutDialogFragment: DialogFragment() {

    //image pick code
    private val IMAGE_PICK_CODE = 1000

    var gifImageLocation: String? = null
    var galleryImageLocation: String? = null
    var imagePath: String? = null
    var mGlobalPreferences: GlobalPreferences? = null

    var workoutImage: ImageView? = null
    var workoutImagePlaceholder: ImageView? = null
    var numberPicker: NumberPicker? = null

    var workoutValue: String? = ""
    var secondsValue: Int? = null
    var workoutImageValue: String? = null

    var workout: WorkoutModel? = null

    interface NewWorkoutDialogListener {
        fun onSaveClick(dialog: DialogFragment, workout: WorkoutModel)
        fun onCancelClick(dialog: DialogFragment)
    }

    var newWorkoutDialogListener: NewWorkoutDialogListener? = null

    companion object {
        fun newInstance(workout: WorkoutModel): NewWorkoutDialogFragment {
            val newWorkoutDialogFragment = NewWorkoutDialogFragment()
            val args = Bundle()
            args.putParcelable("arg_workout", workout)
            newWorkoutDialogFragment.arguments = args
            return newWorkoutDialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val builder = AlertDialog.Builder(this.activity!!)
        mGlobalPreferences = GlobalPreferences(this.activity!!)

        val dialogView = activity?.layoutInflater?.inflate(R.layout.add_workout, null)
        val workoutText = dialogView!!.findViewById<EditText>(R.id.workout_text)
        val giphyView = dialogView.findViewById<GiphyView>(R.id.search_giphy_view)
        val searchGiphyButton = dialogView.findViewById<Button>(R.id.search_giphy_button)
        val searchGiphyLayout = dialogView.findViewById<LinearLayout>(R.id.gif_search_view)
        val closeGiphyButton = dialogView.findViewById<Button>(R.id.close_giphysearch_button)
        val searchGalleryButton = dialogView.findViewById<Button>(R.id.search_gallery_button)
        numberPicker = dialogView.findViewById(R.id.number_picker)
        workoutImage = dialogView.findViewById<ImageView>(R.id.selected_image)
        workoutImagePlaceholder = dialogView.findViewById(R.id.selected_image_placeholder)

        giphyView.initializeView("dc6zaTOxFJmzC", 5 * 1024 * 1024)
        giphyView.setSelectedCallback {

            // Make images visible
            workoutImage!!.visibility = View.VISIBLE
            workoutImagePlaceholder!!.visibility = View.GONE

            gifImageLocation = it.path?.toString()
            Glide.with(this).load(it).into(workoutImage!!)
            galleryImageLocation = null
            imagePath = null
            searchGiphyLayout.visibility = View.GONE
        }

        searchGiphyButton.setOnClickListener {
            searchGiphyLayout.visibility = View.VISIBLE
        }

        closeGiphyButton.setOnClickListener {
            searchGiphyLayout.visibility = View.GONE
            giphyView.invalidate()
        }

        searchGalleryButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this.activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), IMAGE_PICK_CODE)
            } else {
                pickImageFromGallery()
            }
        }

        // TODO: Implement text requirement
        workoutText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                workoutValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                workoutValue = s.toString()
            }

        })

        workout = arguments?.getParcelable("arg_workout")

        if (workout == null) {
            builder.setTitle("Add Workout")
        } else {
            builder.setTitle("Edit Workout")
            numberPicker?.value = workout?.seconds!!
            workoutText.setText(workout?.workoutName)
            if (workout?.workoutImage != null) {
                workoutImage!!.visibility = View.VISIBLE
                workoutImagePlaceholder!!.visibility = View.GONE
                Glide.with(this).load(Uri.fromFile(File(workout?.workoutImage))).into(workoutImage!!)
                workoutImageValue = workout?.workoutImage
            } else {
                workoutImage!!.visibility = View.GONE
                workoutImagePlaceholder!!.visibility = View.VISIBLE
            }
        }

        secondsValue = numberPicker!!.value

        numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
            secondsValue = newVal
        }

        builder.setView(dialogView)
            .setPositiveButton("Save") { dialog, id ->
                mGlobalPreferences!!.setWorkoutModified(true)
                validateImagePath()
                newWorkoutDialogListener?.onSaveClick(this, WorkoutModel(seconds = secondsValue, workoutName = workoutValue, workoutImage = workoutImageValue))
            }
            .setNegativeButton("Close") { dialog, id ->
                newWorkoutDialogListener?.onCancelClick(this)
            }


        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            newWorkoutDialogListener = activity as NewWorkoutDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement NewWorkoutDialogListener")
        }
    }

    private fun validateImagePath() {
        when {
            // For New Workouts
            imagePath != null && gifImageLocation == null -> {
                println("For New Workouts")
                saveSelectedImage(imagePath!!)
                workoutImageValue = galleryImageLocation
            }
            gifImageLocation != null && imagePath == null -> {
                println("For New Workouts")
                workoutImageValue = gifImageLocation
            }
            // For Existing Workouts
            imagePath != null && workoutImageValue != workout?.workoutImage -> {
                println("For Existing Workouts")
                saveSelectedImage(imagePath!!)
                workoutImageValue = galleryImageLocation
            }
            gifImageLocation != null  && workoutImageValue != workout?.workoutImage -> {
                println("For Existing Workouts")
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
                        Glide.with(this).load(uri).into(workoutImage!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    // Set gif image null to avoid issues
                    gifImageLocation = null

                }
            }
        }
    }


}