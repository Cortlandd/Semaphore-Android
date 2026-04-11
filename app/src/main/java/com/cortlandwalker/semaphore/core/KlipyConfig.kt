package com.cortlandwalker.semaphore.core

import com.cortlandwalker.semaphore.BuildConfig

/**
 * App-level Klipy configuration.
 *
 * Klipy 0.1.9 supports direct hardcoded API keys again, so we keep a baked-in
 * fallback here and still allow Gradle/env overrides for local testing.
 */
object KlipyConfig {
    private const val HARDCODED_API_KEY = "fNkmHZ257SEs5hOBeRF6XKSynwsVGodDUzMKzVBObkGgu2cb9vN0YDsHKh7ZyXQl"

    fun resolvedApiKey(): String =
        BuildConfig.KLIPY_API_KEY
            .ifBlank { HARDCODED_API_KEY }
            .trim()
}
