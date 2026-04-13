package com.cortlandwalker.semaphore.data.local.room

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
     * Download the media at [remoteUrl] into app storage for a specific workout and return a
     * file URI (file://...).
     */
    suspend fun cacheFromRemote(remoteUrl: String, ownerId: String): String = withContext(Dispatchers.IO) {
        val mediaDir = File(context.filesDir, "workout_media").apply { mkdirs() }

        val ext = runCatching {
            val raw = Uri.parse(remoteUrl).lastPathSegment ?: ""
            raw.substringAfterLast('.', missingDelimiterValue = "bin")
        }.getOrDefault("bin")

        val destination = File(mediaDir, "$ownerId.$ext")
        val tempFile = File.createTempFile("${ownerId}_", ".$ext", mediaDir)

        try {
            URL(remoteUrl).openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            deleteOwnedLocalImages(ownerId, exceptFileName = tempFile.name)
            if (!tempFile.renameTo(destination)) {
                tempFile.copyTo(destination, overwrite = true)
                tempFile.delete()
            }

            destination.toURI().toString()
        } catch (t: Throwable) {
            tempFile.delete()
            throw t
        }
    }

    suspend fun deleteOwnedLocalImages(ownerId: String, exceptFileName: String? = null) = withContext(Dispatchers.IO) {
        val mediaDir = File(context.filesDir, "workout_media")
        val prefix = "$ownerId."

        mediaDir.listFiles()
            ?.filter { file ->
                file.isFile &&
                    file.name.startsWith(prefix) &&
                    file.name != exceptFileName
            }
            ?.forEach { file -> file.delete() }
    }

    /**
     * Deletes a cached workout media file only if it lives inside the app-managed workout_media
     * directory. This avoids removing unrelated local files referenced by a workout.
     */
    suspend fun deleteCachedLocalImage(imageUri: String?) = withContext(Dispatchers.IO) {
        if (imageUri.isNullOrBlank()) return@withContext

        val uri = runCatching { Uri.parse(imageUri) }.getOrNull() ?: return@withContext
        if (!uri.scheme.equals("file", ignoreCase = true)) return@withContext

        val targetPath = uri.path ?: return@withContext
        val target = File(targetPath)
        val mediaDir = File(context.filesDir, "workout_media")

        val canonicalTarget = runCatching { target.canonicalFile }.getOrNull() ?: return@withContext
        val canonicalMediaDir = runCatching { mediaDir.canonicalFile }.getOrNull() ?: return@withContext

        val mediaDirPath = canonicalMediaDir.path + File.separator
        if (!canonicalTarget.path.startsWith(mediaDirPath)) return@withContext

        if (canonicalTarget.exists()) {
            canonicalTarget.delete()
        }
    }
}
