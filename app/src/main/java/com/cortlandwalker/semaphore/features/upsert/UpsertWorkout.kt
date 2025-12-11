package com.cortlandwalker.semaphore.features.upsert

import com.klipy.sdk.model.MediaItem

data class UpsertWorkoutState(
    val workoutId: String? = null,
    val isLoading: Boolean = false,
    val name: String = "",
    val imageUri: String? = null,
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0,
    val isSaving: Boolean = false,
    val error: String? = null,
    val selectedMediaItem: MediaItem? = null
) {
    val isEdit: Boolean get() = !workoutId.isNullOrBlank()
}

sealed interface UpsertWorkoutAction {
    data class Init(val workoutId: String?) : UpsertWorkoutAction

    data class NameChanged(val value: String) : UpsertWorkoutAction
    data class ImageChanged(val mediaItem: MediaItem) : UpsertWorkoutAction
    data class TimeSet(val h: Int, val m: Int, val s: Int) : UpsertWorkoutAction

    data object GifTapped : UpsertWorkoutAction
    data object SaveClicked : UpsertWorkoutAction
    data object Cancel : UpsertWorkoutAction
}

sealed interface UpsertWorkoutEffect {
    data object Back : UpsertWorkoutEffect
    data class ShowError(val message: String) : UpsertWorkoutEffect
    data object OpenGifPicker : UpsertWorkoutEffect
}

