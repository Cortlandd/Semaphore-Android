package com.cortlandwalker.semaphore.data.local.room

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles caching remote workout GIFs into app-private storage and returns a local file URI.
 */
@Singleton
class WorkoutImageStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Download the GIF at [remoteUrl] into app storage and return a file URI (file://...).
     */
    suspend fun cacheFromRemote(remoteUrl: String): String = withContext(Dispatchers.IO) {
        val gifsDir = File(context.filesDir, "workout_media").apply { mkdirs() }

        val ext = runCatching {
            val raw = Uri.parse(remoteUrl).lastPathSegment ?: ""
            raw.substringAfterLast('.', missingDelimiterValue = "bin")
        }.getOrDefault("bin")

        val fileName = "${remoteUrl.sha256()}.$ext"
        val dest = File(gifsDir, fileName)

        if (dest.exists() && dest.length() > 0L) {
            return@withContext dest.toURI().toString()
        }

        URL(remoteUrl).openStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        dest.toURI().toString()
    }
}

private fun String.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
}
