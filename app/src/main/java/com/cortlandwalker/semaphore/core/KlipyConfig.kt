package com.cortlandwalker.semaphore.core

import com.cortlandwalker.semaphore.BuildConfig

/**
 * App-level Klipy configuration.
 *
 * Prefer supplying `KLIPY_API_KEY` from Gradle properties or environment variables,
 * but this fallback lets local builds hardcode a key during integration testing.
 */
object KlipyConfig {
    private const val HARDCODED_API_KEY = ""

    fun resolvedApiKey(): String =
        BuildConfig.KLIPY_API_KEY
            .ifBlank { HARDCODED_API_KEY }
            .trim()
}
