package com.cortlandwalker.semaphore.features.settings

import com.cortlandwalker.semaphore.monetization.MonetizationUiState

data class SettingsState(
    val version: String = "1.0.0",
    val monetization: MonetizationUiState = MonetizationUiState()
)

sealed interface SettingsAction {
    data object TapAnalytics : SettingsAction
    data object TapBack : SettingsAction
    data object TapFeedback : SettingsAction
    data object TapFAQ : SettingsAction
    data object TapRateApp : SettingsAction
    data object TapRemoveAds : SettingsAction
    data object TapRestorePurchases : SettingsAction
    data class MonetizationUpdated(val monetization: MonetizationUiState) : SettingsAction

    // Bottom Nav Actions
    data object TapWorkouts : SettingsAction
    data object TapTimer : SettingsAction
}

sealed interface SettingsEffect {
    data object NavAnalytics : SettingsEffect
    data object NavBack : SettingsEffect
    data object NavWorkouts : SettingsEffect
    data object NavTimer : SettingsEffect

    // External Intents
    data class SendEmail(val address: String, val subject: String) : SettingsEffect
    data class OpenUrl(val url: String) : SettingsEffect
    data object OpenAppStore : SettingsEffect
    data object NavFAQ : SettingsEffect
    data object LaunchRemoveAdsPurchase : SettingsEffect
    data object RestorePurchases : SettingsEffect
}
