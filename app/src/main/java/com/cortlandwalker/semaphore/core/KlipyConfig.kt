package com.cortlandwalker.semaphore.core

import com.cortlandwalker.semaphore.BuildConfig

/**
 * Centralizes how Semaphore resolves its Klipy API key.
 *
 * Gradle-provided configuration wins when present, but this object also keeps
 * a baked-in fallback so media picking still works in builds where the key is
 * intentionally hardcoded instead of supplied through local properties.
 */
object KlipyConfig {
    private const val HARDCODED_API_KEY = "fNkmHZ257SEs5hOBeRF6XKSynwsVGodDUzMKzVBObkGgu2cb9vN0YDsHKh7ZyXQl"

    fun resolvedApiKey(): String =
        BuildConfig.KLIPY_API_KEY
            .ifBlank { HARDCODED_API_KEY }
            .trim()
}
