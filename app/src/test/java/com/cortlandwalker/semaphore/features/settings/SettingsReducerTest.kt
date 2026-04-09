package com.cortlandwalker.semaphore.features.settings

import com.cortlandwalker.ghettoxide.testBind
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsReducerTest {

    private lateinit var reducer: SettingsReducer
    private lateinit var effects: MutableList<SettingsEffect>

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        effects = mutableListOf()
        reducer = SettingsReducer()
        reducer.testBind(
            initialState = SettingsState(),
            effects = effects
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `TapBack should emit NavBack effect`() = runTest {
        // When
        reducer.accept(SettingsAction.TapBack)

        // Then
        assertThat(effects).contains(SettingsEffect.NavBack)
    }

    @Test
    fun `TapFeedback should emit SendEmail effect`() = runTest {
        // When
        reducer.accept(SettingsAction.TapFeedback)

        // Then
        assertThat(effects.first()).isInstanceOf(SettingsEffect.SendEmail::class.java)
    }

    @Test
    fun `TapFAQ should emit NavFAQ effect`() = runTest {
        // When
        reducer.accept(SettingsAction.TapFAQ)

        // Then
        assertThat(effects).contains(SettingsEffect.NavFAQ)
    }

    @Test
    fun `TapRateApp should emit OpenAppStore effect`() = runTest {
        // When
        reducer.accept(SettingsAction.TapRateApp)

        // Then
        assertThat(effects).contains(SettingsEffect.OpenAppStore)
    }

    @Test
    fun `TapWorkouts should emit NavWorkouts effect`() = runTest {
        // When
        reducer.accept(SettingsAction.TapWorkouts)

        // Then
        assertThat(effects).contains(SettingsEffect.NavWorkouts)
    }

    @Test
    fun `TapTimer should emit NavTimer effect`() = runTest {
        // When
        reducer.accept(SettingsAction.TapTimer)

        // Then
        assertThat(effects).contains(SettingsEffect.NavTimer)
    }
}
