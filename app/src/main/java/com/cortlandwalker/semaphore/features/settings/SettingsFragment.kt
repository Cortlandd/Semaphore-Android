package com.cortlandwalker.semaphore.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import com.cortlandwalker.ghettoxide.ReducerFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : ReducerFragment<SettingsState, SettingsAction, SettingsEffect, SettingsReducer>() {

    @Inject override lateinit var reducer: SettingsReducer
    override val initialState = SettingsState()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = vm.state.collectAsState().value
                SettingsScreen(state, reducer)
            }
        }
    }

    override fun onEffect(effect: SettingsEffect) {
        when (effect) {
            SettingsEffect.NavBack -> findNavController().popBackStack()

            SettingsEffect.NavWorkouts -> {
                findNavController().popBackStack(com.cortlandwalker.semaphore.R.id.workoutListFragment, false)
            }

            SettingsEffect.NavTimer -> {
                // Navigate to timer if it exists, or handle accordingly
            }

            is SettingsEffect.SendEmail -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(effect.address))
                    putExtra(Intent.EXTRA_SUBJECT, effect.subject)
                }
                startActivity(Intent.createChooser(intent, "Send Feedback"))
            }

            is SettingsEffect.OpenUrl -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                startActivity(intent)
            }

            SettingsEffect.OpenAppStore -> {
                val appPackageName = requireContext().packageName
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                } catch (e: android.content.ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }

            SettingsEffect.NavLicenses -> {
                val action = SettingsFragmentDirections.actionSettingsToMarkdown(
                    title = "Open Source Licenses",
                    filename = "licenses.md"
                )
                findNavController().navigate(action)
            }

            SettingsEffect.NavFAQ -> {
                val action = SettingsFragmentDirections.actionSettingsToMarkdown(
                    title = "FAQ",
                    filename = "faq.md"
                )
                findNavController().navigate(action)
            }
        }
    }
}
