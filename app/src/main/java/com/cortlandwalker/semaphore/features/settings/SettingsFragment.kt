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
import com.cortlandwalker.semaphore.R
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
    override val initialState = SettingsState(
        version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    )

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

            SettingsEffect.NavFAQ -> {
                val action = SettingsFragmentDirections.actionSettingsToMarkdown(
                    title = getString(R.string.settings_faq_title),
                    filename = "faq.md"
                )
                findNavController().navigate(action)
            }
            SettingsEffect.LaunchRemoveAdsPurchase -> handleRemoveAdsTapped()
            SettingsEffect.RestorePurchases -> {
                monetizationManager.restorePurchases()
                toast(getString(R.string.toast_checking_previous_purchases))
            }
        }
    }

    private fun handleRemoveAdsTapped() {
        when (val result = monetizationManager.launchRemoveAdsPurchase(requireActivity())) {
            PurchaseLaunchResult.Launched -> Unit
            PurchaseLaunchResult.AlreadyOwned -> toast(getString(R.string.toast_ads_already_removed))
            PurchaseLaunchResult.BillingUnavailable -> toast(getString(R.string.toast_billing_unavailable))
            PurchaseLaunchResult.ProductUnavailable -> toast(getString(R.string.toast_product_still_loading))
            is PurchaseLaunchResult.Failed -> {
                toast(result.debugMessage.ifBlank { getString(R.string.toast_unable_to_start_purchase_flow) })
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
