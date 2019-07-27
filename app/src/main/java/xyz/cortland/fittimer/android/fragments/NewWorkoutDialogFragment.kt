package xyz.cortland.fittimer.android.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import xyz.cortland.fittimer.android.R
import xyz.cortland.fittimer.android.model.WorkoutModel
import java.lang.ClassCastException
import java.lang.NumberFormatException

class NewWorkoutDialogFragment: DialogFragment() {

    interface NewWorkoutDialogListener {
        fun onSaveClick(dialog: DialogFragment, workout: WorkoutModel)
        fun onCancelClick(dialog: DialogFragment)
    }

    var newWorkoutDialogListener: NewWorkoutDialogListener? = null

    companion object {
        fun newInstance(layoutTitle: Int): NewWorkoutDialogFragment {
            val newWorkoutDialogFragment = NewWorkoutDialogFragment()
            var args = Bundle()
            args.putInt("arg_workout", layoutTitle)
            newWorkoutDialogFragment.arguments = args
            return newWorkoutDialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity!!)
        builder.setTitle("Add Workout")

        val dialogView = activity?.layoutInflater?.inflate(R.layout.add_workout, null)
        val seconds = dialogView!!.findViewById<EditText>(R.id.seconds_text)
        val workoutText = dialogView.findViewById<EditText>(R.id.workout_text)

        var workoutValue = ""
        var secondsValue = 0

        seconds.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != "") {
                    secondsValue = Integer.parseInt(s.toString())
                } else {
                    return
                }

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != "") {
                    secondsValue = Integer.parseInt(s.toString())
                } else {
                    return
                }
            }

        })

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

        builder.setView(dialogView)
            .setPositiveButton("Save") { dialog, id ->
                println("Seconds $secondsValue")
                println("Workout $workoutValue")
                newWorkoutDialogListener?.onSaveClick(this, WorkoutModel(secondsValue, workoutValue))
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

}