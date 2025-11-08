package com.cortlandwalker.semaphore.features.workoutlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.cortlandwalker.semaphore.core.helpers.FragmentReducer
import com.cortlandwalker.semaphore.core.helpers.Reducer
import javax.inject.Inject
import androidx.compose.runtime.collectAsState
import androidx.navigation.fragment.findNavController
import com.cortlandwalker.semaphore.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutListFragment : FragmentReducer<WorkoutListState, WorkoutListAction, WorkoutListEffect>() {
    @Inject override lateinit var reducer: WorkoutListReducer
    override val initialState = WorkoutListState()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                reducer.postAction()
                val state = vm.state.collectAsState().value
                WorkoutListScreen(state, reducer)
            }
        }
    }

    override fun onEffect(effect: WorkoutListEffect) {
        when (effect) {
            WorkoutListEffect.NavSettings -> TODO()
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