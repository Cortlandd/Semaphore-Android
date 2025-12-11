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
import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.ghettoxide.ReducerFragment
import com.klipy.klipy_ui.picker.KlipyPickerConfig
import com.klipy.klipy_ui.picker.KlipyPickerDialogFragment
import com.klipy.klipy_ui.picker.KlipyPickerListener
import com.klipy.sdk.model.MediaItem
import com.klipy.sdk.model.MediaType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpsertWorkoutFragment : ReducerFragment<UpsertWorkoutState, UpsertWorkoutAction, UpsertWorkoutEffect, UpsertWorkoutReducer>() {

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
            UpsertWorkoutEffect.OpenGifPicker -> openKlipyPicker()
        }
    }

    private fun openKlipyPicker() {
        val config = KlipyPickerConfig(
            mediaTypes = listOf(MediaType.GIF),
            showTrending = true,
            initialMediaType = MediaType.GIF
        )
        val dialog = KlipyPickerDialogFragment.newInstance(config)

        dialog.listener = object : KlipyPickerListener {
            override fun onMediaSelected(
                item: MediaItem,
                searchTerm: String?
            ) {
                reducer.postAction(UpsertWorkoutAction.ImageChanged(mediaItem = item))
            }

            override fun onDismissed(lastContentType: MediaType?) {

            }

            override fun didSearchTerm(term: String) {

            }

        }

        dialog.show(childFragmentManager, "klipy_picker")
    }
}
