package com.cortlandwalker.semaphore.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import com.cortlandwalker.semaphore.BuildConfig
import com.cortlandwalker.ghettoxide.ReducerContent
import com.cortlandwalker.ghettoxide.ReducerFragment
import com.cortlandwalker.semaphore.monetization.MonetizationManager
import com.cortlandwalker.semaphore.monetization.PurchaseLaunchResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : ReducerFragment<SettingsState, SettingsAction, SettingsEffect, SettingsReducer>() {

    @Inject override lateinit var reducer: SettingsReducer
    @Inject lateinit var monetizationManager: MonetizationManager
    override val initialState = SettingsState(version = BuildConfig.VERSION_NAME)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        monetizationManager.start()
        return ComposeView(requireContext()).apply {
            setContent {
                val monetizationState = monetizationManager.uiState.collectAsStateWithLifecycle().value
                LaunchedEffect(monetizationState) {
                    reducer.postAction(SettingsAction.MonetizationUpdated(monetizationState))
                }
                ReducerContent { state, reducer ->
                    SettingsScreen(state, reducer)
                }
            }
        }
    }

    override fun onEffect(effect: SettingsEffect) {
        when (effect) {
            SettingsEffect.NavAnalytics -> {
                val action = SettingsFragmentDirections.actionSettingsToAnalytics()
                findNavController().navigate(action)
            }
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
            SettingsEffect.LaunchRemoveAdsPurchase -> handleRemoveAdsTapped()
            SettingsEffect.RestorePurchases -> {
                monetizationManager.restorePurchases()
                toast("Checking Google Play for previous purchases.")
            }
        }
    }

    private fun handleRemoveAdsTapped() {
        when (val result = monetizationManager.launchRemoveAdsPurchase(requireActivity())) {
            PurchaseLaunchResult.Launched -> Unit
            PurchaseLaunchResult.AlreadyOwned -> toast("Ads have already been removed on this account.")
            PurchaseLaunchResult.BillingUnavailable -> toast("Google Play billing is unavailable right now.")
            PurchaseLaunchResult.ProductUnavailable -> toast("The remove ads option is still loading. Try again in a moment.")
            is PurchaseLaunchResult.Failed -> {
                toast(result.debugMessage.ifBlank { "Unable to start the purchase flow." })
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
