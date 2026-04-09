package com.cortlandwalker.semaphore.features.settings

import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class SettingsScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun `SettingsScreen initial state`() {
        paparazzi.snapshot {
            val dummyState = SettingsState(
                version = "1.0.0 (1)"
            )

            val reducer = SettingsReducer()

            SettingsScreen(
                state = dummyState,
                reducer = reducer
            )
        }
    }
}
