package com.cortlandwalker.semaphore.features.upsert

import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.WorkoutImageStore
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import java.util.UUID
import javax.inject.Inject
import kotlin.text.lowercase
import kotlin.text.startsWith

class UpsertWorkoutReducer @Inject constructor(
    private val repo: WorkoutRepository,
    private val imageStore: WorkoutImageStore
) : Reducer<UpsertWorkoutState, UpsertWorkoutAction, UpsertWorkoutEffect>() {

    private var original: Workout? = null  // only set in edit mode

    override suspend fun process(action: UpsertWorkoutAction) {
        when (action) {
            is UpsertWorkoutAction.Init -> {
                state { it.copy(workoutId = action.workoutId) }

                if (action.workoutId.isNullOrBlank()) {
                    state { it.copy(viewDisplayMode = ViewDisplayMode.Empty, error = null) }
                } else {
                    state { it.copy(viewDisplayMode = ViewDisplayMode.Loading, error = null) }
                    val w = runCatching { repo.getById(action.workoutId) }.getOrNull()
                    if (w == null) {
                        state {
                            it.copy(
                                viewDisplayMode = ViewDisplayMode.Error,
                                error = "Workout not found"
                            )
                        }
                    } else {
                        original = w
                        state {
                            it.copy(
                                viewDisplayMode = ViewDisplayMode.Content,
                                name = w.name,
                                imageUri = w.displayImageUri,
                                remoteImageUri = w.sourceImageUri,
                                hours = w.hours, minutes = w.minutes, seconds = w.seconds
                            )
                        }
                    }
                }
            }

            is UpsertWorkoutAction.NameChanged -> state { it.copy(name = action.value) }
            is UpsertWorkoutAction.ImageChanged -> {
                val item = action.mediaItem
                val url = item.highQualityMetaData?.url ?: item.lowQualityMetaData?.url
                state {
                    it.copy(
                        selectedMediaItem = item,
                        imageUri = url,
                        remoteImageUri = url
                    )
                }
            }
            is UpsertWorkoutAction.TimeSet -> state { it.copy(hours = action.h, minutes = action.m, seconds = action.s) }

            UpsertWorkoutAction.GifTapped -> emit(UpsertWorkoutEffect.OpenGifPicker)

            UpsertWorkoutAction.SaveClicked -> {
                val s = currentState
                if (s.name.isBlank() || (s.hours + s.minutes + s.seconds) == 0) {
                    emit(UpsertWorkoutEffect.ShowError("Enter a name and a non-zero time"))
                    return
                }

                state { it.copy(isSaving = true) }

                val resolvedImage = resolveImageForSave(s)

                if (!s.isEdit) {
                    // Create
                    val position = runCatching { repo.maxPosition() }.getOrDefault(-1) + 1
                    val new = Workout(
                        id = UUID.randomUUID().toString(),
                        createdAt = System.currentTimeMillis(),
                        name = s.name.trim(),
                        imageUri = resolvedImage.localImageUri,
                        hours = s.hours, minutes = s.minutes, seconds = s.seconds,
                        position = position,
                        orderId = 0,
                        remoteImageUri = resolvedImage.remoteImageUri
                    )
                    runCatching { repo.insert(new) }
                        .onFailure { e ->
                            state { it.copy(isSaving = false) }
                            emit(UpsertWorkoutEffect.ShowError(e.message ?: "Failed to save")); return
                        }
                    emit(UpsertWorkoutEffect.Back)
                } else {
                    // Update
                    val base = original ?: run {
                        state { it.copy(isSaving = false) }
                        emit(UpsertWorkoutEffect.ShowError("Internal error")); return
                    }
                    val updated = base.copy(
                        name = s.name.trim(),
                        imageUri = resolvedImage.localImageUri,
                        hours = s.hours, minutes = s.minutes, seconds = s.seconds,
                        remoteImageUri = resolvedImage.remoteImageUri
                        // keep position/orderId
                    )
                    runCatching { repo.update(updated) }
                        .onFailure { e ->
                            state { it.copy(isSaving = false) }
                            emit(UpsertWorkoutEffect.ShowError(e.message ?: "Failed to update")); return
                        }

                    emit(UpsertWorkoutEffect.Back)
                }
            }

            UpsertWorkoutAction.Cancel -> emit(UpsertWorkoutEffect.Back)
        }
    }

    override fun onLoadAction(): UpsertWorkoutAction = UpsertWorkoutAction.Init(currentState.workoutId)

    private suspend fun resolveImageForSave(state: UpsertWorkoutState): ResolvedWorkoutImage {
        val imageUri = state.imageUri?.ifBlank { null }
        val remoteImageUri = state.remoteImageUri?.ifBlank { null }

        if (remoteImageUri != null) {
            return ResolvedWorkoutImage(
                localImageUri = runCatching { imageStore.cacheFromRemote(remoteImageUri) }
                    .getOrElse { imageUri?.takeUnless(::isRemoteUri) },
                remoteImageUri = remoteImageUri
            )
        }

        if (imageUri == null) {
            return ResolvedWorkoutImage(localImageUri = null, remoteImageUri = null)
        }

        if (isRemoteUri(imageUri)) {
            return ResolvedWorkoutImage(
                localImageUri = runCatching { imageStore.cacheFromRemote(imageUri) }.getOrNull(),
                remoteImageUri = imageUri
            )
        }

        return ResolvedWorkoutImage(localImageUri = imageUri, remoteImageUri = null)
    }

    private fun isRemoteUri(uri: String): Boolean {
        return uri.startsWith("http://", ignoreCase = true) || uri.startsWith("https://", ignoreCase = true)
    }

    private data class ResolvedWorkoutImage(
        val localImageUri: String?,
        val remoteImageUri: String?
    )
}
