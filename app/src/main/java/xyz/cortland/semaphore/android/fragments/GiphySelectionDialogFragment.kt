package xyz.cortland.semaphore.android.fragments

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import xyz.cortland.semaphore.android.R
import xyz.klinker.giphy.GiphyView

class GiphySelectionDialogFragment: DialogFragment() {

    var giphyView: GiphyView? = null

    interface GiphyImageSelectedInterface {
        fun onGifSelected(dialogFragment: DialogFragment, imageUri: Uri)
    }

    var giphyImageSelectedInterface: GiphyImageSelectedInterface? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(context!!, R.style.AlertDialogTheme)

        val dialogView = activity?.layoutInflater?.inflate(R.layout.search_giphy_dialog, null)

        giphyView = dialogView!!.findViewById<GiphyView>(R.id.search_giphy_view)
        giphyView?.initializeView("dc6zaTOxFJmzC", 5 * 1024 * 1024)

        giphyView?.setSelectedCallback {
            giphyImageSelectedInterface?.onGifSelected(this, it)
        }

        builder.setView(dialogView)

        return builder.create()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (giphyView != null) {
            giphyView!!.invalidate()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // This actually works...
            giphyImageSelectedInterface = fragmentManager!!.fragments.get(0) as GiphyImageSelectedInterface
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement GiphySelectionDialogFragment")
        }
    }

}