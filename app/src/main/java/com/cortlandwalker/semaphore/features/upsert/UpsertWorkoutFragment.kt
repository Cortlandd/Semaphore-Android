package com.cortlandwalker.semaphore.features.upsert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cortlandwalker.semaphore.core.helpers.FragmentReducer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpsertWorkoutFragment : FragmentReducer<UpsertWorkoutState, UpsertWorkoutAction, UpsertWorkoutEffect>() {

    @Inject override lateinit var reducer: UpsertWorkoutReducer

    private val args by navArgs<UpsertWorkoutFragmentArgs>()

    override val initialState: UpsertWorkoutState by lazy {
        UpsertWorkoutState(workoutId = args.workoutId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.postAction(UpsertWorkoutAction.Init(args.workoutId))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                val s = vm.state.collectAsState().value
                UpsertWorkoutScreen(
                    state = s,
                    reducer = reducer
                )
            }
        }

    override fun onEffect(effect: UpsertWorkoutEffect) {
        when (effect) {
            UpsertWorkoutEffect.Back -> findNavController().popBackStack()
            is UpsertWorkoutEffect.ShowError ->
                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
            UpsertWorkoutEffect.OpenGifPicker -> {
                // Launch your GIF picker; send result back:
                // reducer.postAction(UpsertWorkoutAction.ImageChanged(selectedUri))
            }
        }
    }

//    private fun openGiphyPicker() {
//        val settings = GPHSettings().apply {
//            // tweak as desired
//            stickerColumnCount = 3
//            mediaTypeConfig = arrayOf(GPHContentType.gif, GPHContentType.recents)
//        }
//
//        val dialog = GiphyDialogFragment.newInstance(settings)
//        dialog.gifSelectionListener = object : GiphyDialogFragment.GifSelectionListener {
//            override fun onGifSelected(
//                media: Media,
//                searchTerm: String?,
//                selectedContentType: GPHContentType
//            ) {
//                // Prefer a reasonably sized rendition
//                val url =
//                    media.images?.downsized?.gifUrl
//                        ?: media.images?.fixedWidth?.gifUrl
//                        ?: media.images?.original?.gifUrl
//                        ?: return
//
//                //reducer.postAction(UpsertWorkoutAction.GifSelected(url))
//            }
//
//            override fun didSearchTerm(term: String) {
//
//            }
//
//            override fun onDismissed(selectedContentType: GPHContentType) {
//                // no-op
//            }
//        }
//        dialog.show(childFragmentManager, "giphy_dialog")
//    }
}
