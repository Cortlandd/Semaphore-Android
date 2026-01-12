package com.cortlandwalker.semaphore.features.settings

import com.cortlandwalker.ghettoxide.Reducer
import javax.inject.Inject

class SettingsReducer @Inject constructor() :
    Reducer<SettingsState, SettingsAction, SettingsEffect>() {

    override fun onLoadAction(): SettingsAction? = null

    override suspend fun process(action: SettingsAction) {
        when (action) {
            SettingsAction.TapAnalytics -> emit(SettingsEffect.NavAnalytics)
            SettingsAction.TapBack -> emit(SettingsEffect.NavBack)

            SettingsAction.TapFeedback -> {
                // Pre-fill email intent
                emit(SettingsEffect.SendEmail(
                    address = "your-email@example.com",
                    subject = "Semaphore Feedback"
                ))
            }

            SettingsAction.TapLicenses -> emit(SettingsEffect.NavLicenses)

            SettingsAction.TapFAQ -> emit(SettingsEffect.NavFAQ)

            SettingsAction.TapRateApp -> emit(SettingsEffect.OpenAppStore)

            SettingsAction.TapWorkouts -> emit(SettingsEffect.NavWorkouts)
            SettingsAction.TapTimer -> emit(SettingsEffect.NavTimer)
        }
    }
}
