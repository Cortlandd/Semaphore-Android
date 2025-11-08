package com.cortlandwalker.semaphore.features.upsert

import com.cortlandwalker.semaphore.core.helpers.Reducer
import com.cortlandwalker.semaphore.data.local.room.WorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import java.util.UUID
import javax.inject.Inject

class UpsertWorkoutReducer @Inject constructor(
    private val repo: WorkoutRepository
) : Reducer<UpsertWorkoutState, UpsertWorkoutAction, UpsertWorkoutEffect>() {

    private var original: Workout? = null  // only set in edit mode

    override suspend fun process(action: UpsertWorkoutAction) {
        when (action) {
            is UpsertWorkoutAction.Init -> {
                // Seed the workoutId into state (so screens see mode immediately)
                state { it.copy(workoutId = action.workoutId) }

                if (action.workoutId.isNullOrBlank()) {
                    // Add mode: nothing to load
                    state { it.copy(isLoading = false, error = null) }
                } else {
                    // Edit mode: one-shot fetch
                    state { it.copy(isLoading = true, error = null) }
                    val w = runCatching { repo.getById(action.workoutId) }.getOrNull()
                    if (w == null) {
                        state { it.copy(isLoading = false, error = "Workout not found") }
                    } else {
                        original = w
                        state {
                            it.copy(
                                isLoading = false,
                                name = w.name,
                                imageUri = w.imageUri?.ifBlank { null },
                                hours = w.hours, minutes = w.minutes, seconds = w.seconds
                            )
                        }
                    }
                }
            }

            is UpsertWorkoutAction.NameChanged -> state { it.copy(name = action.value) }
            is UpsertWorkoutAction.ImageChanged -> state { it.copy(imageUri = action.uri) }
            is UpsertWorkoutAction.TimeSet -> state { it.copy(hours = action.h, minutes = action.m, seconds = action.s) }

            UpsertWorkoutAction.GifTapped -> emit(UpsertWorkoutEffect.OpenGifPicker)

            UpsertWorkoutAction.SaveClicked -> {
                val s = read()
                if (s.name.isBlank() || (s.hours + s.minutes + s.seconds) == 0) {
                    emit(UpsertWorkoutEffect.ShowError("Enter a name and a non-zero duration"))
                    return
                }
                state { it.copy(isSaving = true, isLoading = true) }

                if (!s.isEdit) {
                    // Create
                    val position = runCatching { repo.maxPosition() }.getOrDefault(-1) + 1
                    val new = Workout(
                        id = UUID.randomUUID().toString(),
                        createdAt = System.currentTimeMillis(),
                        name = s.name.trim(),
                        imageUri = s.imageUri ?: "",
                        hours = s.hours, minutes = s.minutes, seconds = s.seconds,
                        position = position,
                        orderId = 0
                    )
                    runCatching { repo.insert(new) }
                        .onFailure { e ->
                            state { it.copy(isSaving = false, isLoading = false) }
                            emit(UpsertWorkoutEffect.ShowError(e.message ?: "Failed to save")); return
                        }
                    emit(UpsertWorkoutEffect.Back)
                } else {
                    // Update
                    val base = original ?: run {
                        state { it.copy(isSaving = false, isLoading = false) }
                        emit(UpsertWorkoutEffect.ShowError("Internal error")); return
                    }
                    val updated = base.copy(
                        name = s.name.trim(),
                        imageUri = s.imageUri ?: "",
                        hours = s.hours, minutes = s.minutes, seconds = s.seconds
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
}
