package com.cortlandwalker.semaphore.features.settings

data class SettingsState(
    val version: String = "1.0.0"
)

sealed interface SettingsAction {
    data object TapBack : SettingsAction
    data object TapFeedback : SettingsAction    data object TapLicenses : SettingsAction
    data object TapFAQ : SettingsAction
    data object TapRateApp : SettingsAction

    // Bottom Nav Actions
    data object TapWorkouts : SettingsAction
    data object TapTimer : SettingsAction
}

sealed interface SettingsEffect {
    data object NavBack : SettingsEffect
    data object NavWorkouts : SettingsEffect
    data object NavTimer : SettingsEffect

    // External Intents
    data class SendEmail(val address: String, val subject: String) : SettingsEffect
    data class OpenUrl(val url: String) : SettingsEffect // For FAQ if web-based
    data object OpenAppStore : SettingsEffect
    data object NavLicenses : SettingsEffect // Internal navigation to Markdown viewer
    data object NavFAQ : SettingsEffect // Internal navigation to Markdown viewer
}
