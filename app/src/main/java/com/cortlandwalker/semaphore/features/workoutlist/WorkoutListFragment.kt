package com.cortlandwalker.semaphore.features.workoutlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javax.inject.Inject
import androidx.navigation.fragment.findNavController
import com.cortlandwalker.ghettoxide.ReducerContent
import com.cortlandwalker.ghettoxide.ReducerFragment
import com.cortlandwalker.semaphore.monetization.MonetizationManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutListFragment : ReducerFragment<WorkoutListState, WorkoutListAction, WorkoutListEffect, WorkoutListReducer>() {
    @Inject override lateinit var reducer: WorkoutListReducer
    @Inject lateinit var monetizationManager: MonetizationManager
    override val initialState = WorkoutListState()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        monetizationManager.start()
        return ComposeView(requireContext()).apply {
            setContent {
                val monetizationState = monetizationManager.uiState.collectAsStateWithLifecycle().value
                LaunchedEffect(monetizationState.canShowBannerAds) {
                    reducer.postAction(
                        WorkoutListAction.BannerAdVisibilityChanged(monetizationState.canShowBannerAds)
                    )
                }
                ReducerContent { state, reducer ->
                    WorkoutListScreen(state, reducer)
                }
            }
        }
    }

    override fun onEffect(effect: WorkoutListEffect) {
        when (effect) {
            WorkoutListEffect.NavSettings -> {
                findNavController().navigate(WorkoutListFragmentDirections.actionWorkoutListFragmentToSettingsFragment())
            }
            WorkoutListEffect.NavAddWorkout -> {
                findNavController().navigate(WorkoutListFragmentDirections.actionWorkoutListFragmentToUpsertWorkoutFragment(null))
            }

            is WorkoutListEffect.NavEditWorkout -> {
                val action = WorkoutListFragmentDirections.actionWorkoutListFragmentToUpsertWorkoutFragment(effect.workoutId)
                findNavController().navigate(action)
            }
        }
    }
}
