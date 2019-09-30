package xyz.cortland.fittimer.android.fragments

import android.Manifest
import android.app.Activity
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
import xyz.cortland.fittimer.android.model.ActivityModel
import xyz.klinker.giphy.GiphyView
import java.lang.ClassCastException
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.shawnlin.numberpicker.NumberPicker
import xyz.cortland.fittimer.android.SemaphoreApp
import xyz.cortland.fittimer.android.database.ActivityDatabase
import xyz.cortland.fittimer.android.extensions.speakText
import xyz.cortland.fittimer.android.helpers.IMAGE_PICK_CODE
import xyz.cortland.fittimer.android.utils.GlobalPreferences
import xyz.cortland.fittimer.android.utils.ImageFilePath
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


// TODO: (MAYBE) Implemented an EditActivityDialogFragment Dialog Class
class NewActivityDialogFragment: DialogFragment() {

    var gifImageLocation: String? = null
    var galleryImageLocation: String? = null
    var imagePath: String? = null
    var mGlobalPreferences: GlobalPreferences? = null

    var activityImage: ImageView? = null
    var activityImagePlaceholder: ImageView? = null
    var hoursNumberPicker: NumberPicker? = null
    var minutesNumberPicker: NumberPicker? = null
    var secondsNumberPicker: NumberPicker? = null
    var activitySpeech: SwitchCompat? = null
    var searchGiphyLayout: LinearLayout? = null
    var giphyView: GiphyView? = null
    var activityText: EditText? = null
    var fullControls: NestedScrollView? = null

    var activityValue: String? = ""
    var hoursValue: Int? = null
    var minutesValue: Int? = null
    var secondsValue: Int? = null
    var activityImageValue: String? = null
    var activitySpeechValue: Int? = 1

    var activityModel: ActivityModel? = null
    var activityModelId: Int? = null

    var positiveButton: Button? = null
    var negativeButton: Button? = null

    interface NewActivityDialogListener {
        fun onSaveClick(dialog: DialogFragment, activityModel: ActivityModel)
        fun onCancelClick(dialog: DialogFragment)
    }

    var newActivityDialogListener: NewActivityDialogListener? = null

    companion object {
        fun newInstance(activityModel: ActivityModel, id: Int): NewActivityDialogFragment {
            val newActivityDialogFragment = NewActivityDialogFragment()
            val args = Bundle()
            args.putParcelable("arg_activity", activityModel)
            args.putInt("arg_activity_id", id)
            newActivityDialogFragment.arguments = args
            return newActivityDialogFragment
        }
    }

    override fun onStart() {
        super.onStart()
        // Because my goofass
        val d: AlertDialog = dialog as AlertDialog
        if (d != null) {
            positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = d.getButton(Dialog.BUTTON_NEGATIVE)
            if (activityText!!.text.toString().length >= 2) {
                positiveButton?.isEnabled = true
            } else {
                positiveButton?.isEnabled = false
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val builder = AlertDialog.Builder(this.activity!!, R.style.AlertDialogTheme)
        mGlobalPreferences = SemaphoreApp.applicationContext().preferences

        val dialogView = activity?.layoutInflater?.inflate(R.layout.add_activity, null)

        activityModel = arguments?.getParcelable("arg_activity")
        activityModelId = arguments?.getInt("arg_activity_id")

        setupView(dialogView!!)


        if (activityModel == null) {
            builder.setTitle(R.string.add_activity)
        } else {
            builder.setTitle(R.string.edit_activity)
            hoursValue = activityModel?.hours!!
            hoursNumberPicker?.value = activityModel?.hours!!
            minutesValue = activityModel?.minutes!!
            minutesNumberPicker?.value = activityModel?.minutes!!
            secondsValue = activityModel?.seconds!!
            secondsNumberPicker?.value = activityModel?.seconds!!
            activityText?.setText(activityModel?.activityName)
            if (activityModel!!.activitySpeech == 1) {
                activitySpeechValue = 1
                activitySpeech!!.isChecked = true
            } else {
                activitySpeechValue = 0
                activitySpeech!!.isChecked = false
            }

            if (activityModel?.activityImage != null) {
                activityImage!!.visibility = View.VISIBLE
                activityImagePlaceholder!!.visibility = View.GONE
                Glide.with(this).load(Uri.fromFile(File(activityModel?.activityImage))).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.circular_progress_bar).into(activityImage!!)
                activityImageValue = activityModel?.activityImage
            } else {
                activityImage!!.visibility = View.GONE
                activityImagePlaceholder!!.visibility = View.VISIBLE
            }
        }

        builder.setView(dialogView)
            .setPositiveButton(R.string.save) { dialog, id ->
                mGlobalPreferences!!.activityModified = true
                validateImagePath()
                newActivityDialogListener?.onSaveClick(this, ActivityModel(hours = hoursValue, minutes = minutesValue, seconds = secondsValue, activityName = activityValue, activityImage = activityImageValue, activitySpeech = activitySpeechValue))
            }
            .setNegativeButton(R.string.close) { dialog, id ->
                // TODO: If cancel is selected and image isn't null, delete anything created
                newActivityDialogListener?.onCancelClick(this)
            }


        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            newActivityDialogListener = activity as NewActivityDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement NewActivityDialogListener")
        }
    }

    fun setupView(dialogView: View) {

        fullControls = dialogView.findViewById(R.id.activity_content_view)
        activityText = dialogView.findViewById(R.id.activity_text)
        giphyView = dialogView.findViewById(R.id.search_giphy_view)
        val closeGiphyButton = dialogView.findViewById<Button>(R.id.close_giphysearch_button)
        searchGiphyLayout = dialogView.findViewById(R.id.gif_search_view)
        activitySpeech = dialogView.findViewById(R.id.activity_to_speech)
        hoursNumberPicker = dialogView.findViewById(R.id.hours_number_picker)
        minutesNumberPicker = dialogView.findViewById(R.id.minutes_number_picker)
        secondsNumberPicker = dialogView.findViewById(R.id.seconds_number_picker)
        activityImage = dialogView.findViewById<ImageView>(R.id.selected_image)
        activityImagePlaceholder = dialogView.findViewById(R.id.selected_image_placeholder)

        // TODO: Get API Key from Giphy
        // TODO: Create separate Alert Dialog for this with a callback
        giphyView?.setSelectedCallback {

            // Make images visible
            activityImage!!.visibility = View.VISIBLE
            activityImagePlaceholder!!.visibility = View.GONE

            gifImageLocation = it.path?.toString()
            Glide.with(this).load(it).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.circular_progress_bar).into(activityImage!!)
            galleryImageLocation = null
            imagePath = null
            searchGiphyLayout?.visibility = View.GONE
            showControlButtons()
        }


        activityText!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                activityValue = s.toString()
                positiveButton?.isEnabled = s.toString().length >= 2
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                activityValue = s.toString()
                positiveButton?.isEnabled = s.toString().length >= 2
            }

        })

        closeGiphyButton.setOnClickListener {
            searchGiphyLayout?.visibility = View.GONE
            giphyView?.invalidate()
            showControlButtons()
        }

        activitySpeech!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activitySpeechValue = 1
                if (activityText?.text.toString() != "") {
                    context?.speakText(activityText?.text.toString())
                }
            } else {
                activitySpeechValue = 0
            }
        }

        hoursValue = hoursNumberPicker!!.value
        hoursNumberPicker!!.setOnValueChangedListener { picker, oldVal, newVal ->
            hoursValue = newVal
        }

        minutesValue = minutesNumberPicker!!.value
        minutesNumberPicker!!.setOnValueChangedListener { picker, oldVal, newVal ->
            minutesValue = newVal
        }

        secondsValue = secondsNumberPicker!!.value
        secondsNumberPicker!!.setOnValueChangedListener { picker, oldVal, newVal ->
            secondsValue = newVal
        }

        activityImage?.setOnClickListener {
            tapActivityImage()
        }
        activityImagePlaceholder?.setOnClickListener {
            tapPlaceholderImage()
        }

    }

    private fun validateImagePath() {
        when {
            // For New Activities
            imagePath != null && gifImageLocation == null -> {
                saveSelectedImage(imagePath!!)
                activityImageValue = galleryImageLocation
            }
            gifImageLocation != null && imagePath == null -> {
                activityImageValue = gifImageLocation
            }
            // For Existing Activities
            imagePath != null && activityImageValue != activityModel?.activityImage -> {
                saveSelectedImage(imagePath!!)
                activityImageValue = galleryImageLocation
            }
            gifImageLocation != null  && activityImageValue != activityModel?.activityImage -> {
                activityImageValue = gifImageLocation
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

                    activityImage!!.visibility = View.VISIBLE
                    activityImagePlaceholder!!.visibility = View.GONE

                    try {
                        Glide.with(this).load(uri).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.circular_progress_bar).into(activityImage!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    // Set gif image null to avoid issues
                    gifImageLocation = null

                }
            }
        }
    }

    fun tapActivityImage() {
        val builder = AlertDialog.Builder(this.activity!!)
        if (activityModel == null) {
            builder.setTitle(R.string.add_activity_image)
        } else {
            builder.setTitle(R.string.edit_activity_image)
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
                    if (activityModel != null) {
                        // Delete File associated from cache
                        if (activityModel?.activityImage != null) {
                            File(activityModel?.activityImage).delete()
                        }
                        Glide.with(this.activity!!).clear(activityImage!!)
                        // Glide clearing Imageview
                        // Update in the database
                        ActivityDatabase(this.activity!!, null).updateActivityImage(activityModelId!!)
                        // Update activityModel image views
                        activityImagePlaceholder!!.visibility = View.VISIBLE
                        activityImage!!.visibility = View.GONE
                        // Preferences to indicate image removed
                        mGlobalPreferences!!.currentImageRemoved = true
                    } else {

                        if (gifImageLocation != null) {

                            val gif = File(gifImageLocation)
                            if (gif.exists()) {
                                gif.delete()
                                Glide.with(this.activity!!).clear(activityImage!!)
                            }

                            activityImage?.visibility = View.GONE
                            activityImagePlaceholder?.visibility = View.VISIBLE
                        }

                        if (imagePath != null) {
                            val img = File(imagePath)
                            if (img.exists()) {
                                img.delete()
                                Glide.with(this.activity!!).clear(activityImage!!)
                            }

                            activityImage?.visibility = View.GONE
                            activityImagePlaceholder?.visibility = View.VISIBLE
                        }

                    }

                }
            }
        }.create().show()
    }

    fun tapPlaceholderImage() {
        val builder = AlertDialog.Builder(this.activity!!)
        if (activityModel?.activityImage == null) {
            builder.setTitle(R.string.add_activity_image)
        } else {
            builder.setTitle(R.string.edit_activity_image)
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