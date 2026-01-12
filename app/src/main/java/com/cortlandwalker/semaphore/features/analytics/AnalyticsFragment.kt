package com.cortlandwalker.semaphore.features.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import com.cortlandwalker.ghettoxide.ReducerFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AnalyticsFragment : ReducerFragment<AnalyticsState, AnalyticsAction, AnalyticsEffect, AnalyticsReducer>() {

    @Inject
    override lateinit var reducer: AnalyticsReducer
    override val initialState = AnalyticsState()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = vm.state.collectAsState().value
                AnalyticsScreen(
                    state = state,
                    reducer = reducer
                )
            }
        }
    }

    override fun onEffect(effect: AnalyticsEffect) {
        when (effect) {
            AnalyticsEffect.NavBack -> findNavController().popBackStack()
        }
    }
}
