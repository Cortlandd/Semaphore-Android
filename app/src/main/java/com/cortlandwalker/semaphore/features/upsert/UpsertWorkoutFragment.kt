package com.cortlandwalker.semaphore.features.upsert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cortlandwalker.semaphore.core.KlipyConfig
import com.cortlandwalker.ghettoxide.ReducerContent
import com.cortlandwalker.ghettoxide.ReducerFragment
import com.klipy.klipy_ui.picker.KlipyPickerConfig
import com.klipy.klipy_ui.picker.KlipyPickerDialogFragment
import com.klipy.klipy_ui.picker.KlipyPickerListener
import com.klipy.sdk.model.MediaItem
import com.klipy.sdk.model.MediaType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpsertWorkoutFragment : ReducerFragment<UpsertWorkoutState, UpsertWorkoutAction, UpsertWorkoutEffect, UpsertWorkoutReducer>(), KlipyPickerListener {

    @Inject override lateinit var reducer: UpsertWorkoutReducer

    private val args by navArgs<UpsertWorkoutFragmentArgs>()

    override val initialState: UpsertWorkoutState by lazy {
        UpsertWorkoutState(workoutId = args.workoutId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                ReducerContent { state, reducer ->
                    UpsertWorkoutScreen(state, reducer)
                }
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
        val klipyApiKey = KlipyConfig.resolvedApiKey()

        if (klipyApiKey.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Klipy is not configured for this build. Set KLIPY_API_KEY before opening the picker.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val config = KlipyPickerConfig(
            mediaTypes = listOf(MediaType.GIF),
            showTrending = true,
            initialMediaType = MediaType.GIF
        )
        val dialog = KlipyPickerDialogFragment.newInstance(config)
            .apply { listener = this@UpsertWorkoutFragment }

        dialog.show(childFragmentManager, "klipy_picker")
    }

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
