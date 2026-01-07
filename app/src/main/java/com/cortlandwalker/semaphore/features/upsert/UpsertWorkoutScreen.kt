package com.cortlandwalker.semaphore.features.upsert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository
import com.cortlandwalker.semaphore.data.local.room.WorkoutImageStore
import com.klipy.klipy_ui.components.MediaItemPreview
import com.klipy.sdk.model.MediaItem
import com.seo4d696b75.compose.material3.picker.NumberPicker
import kotlinx.collections.immutable.toPersistentList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertWorkoutScreen(
    state: UpsertWorkoutState,
    reducer: UpsertWorkoutReducer
) {
    // local sheet + live preview
    var previewH by remember { mutableIntStateOf(state.hours) }
    var previewM by remember { mutableIntStateOf(state.minutes) }
    var previewS by remember { mutableIntStateOf(state.seconds) }

    // When loading finishes (or new state arrives) and the sheet isn't open,
    // keep the preview synced with state so header shows the loaded values.
    LaunchedEffect(state.hours, state.minutes, state.seconds, state.viewDisplayMode) {
        if (state.viewDisplayMode != ViewDisplayMode.Loading) {
            previewH = state.hours
            previewM = state.minutes
            previewS = state.seconds
        }
    }

    val title = if (state.isEdit) "Edit Workout" else "Add Workout"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { reducer.postAction(UpsertWorkoutAction.Cancel) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->

        when (state.viewDisplayMode) {
            ViewDisplayMode.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(72.dp)
                            .padding(4.dp),
                        trackColor = Color(0x6200EE).copy(alpha = 0.25f),
                    )
                }
            }
            ViewDisplayMode.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(inner),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${state.error ?: "Unknown error"}")
                }
            }
            // Use Empty for Add, Content for Edit -> Both show the form
            ViewDisplayMode.Empty,
            ViewDisplayMode.Content -> {
                UpsertContent(
                    state = state,
                    reducer = reducer,
                    previewH = previewH,
                    previewM = previewM,
                    previewS = previewS,
                    onPreviewUpdate = { h, m, s ->
                        previewH = h; previewM = m; previewS = s
                    },
                    modifier = Modifier.padding(inner)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpsertContent(
    state: UpsertWorkoutState,
    reducer: UpsertWorkoutReducer,
    previewH: Int,
    previewM: Int,
    previewS: Int,
    onPreviewUpdate: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val controlsEnabled = !state.isSaving && state.viewDisplayMode != ViewDisplayMode.Loading
    val primaryText = if (state.isEdit) "Update" else "Save"

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Tap to choose GIF (Fragment handles effect)
            MediaHeader(
                mediaItem = state.selectedMediaItem,
                imageUri = state.imageUri,
                onTap = { if (controlsEnabled) reducer.postAction(UpsertWorkoutAction.GifTapped) }
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { reducer.postAction(UpsertWorkoutAction.NameChanged(it)) },
                label = { Text("Name") },
                singleLine = true,
                enabled = controlsEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                NumberPicker(
                    value = previewH,
                    range = (0..23).toPersistentList(),
                    onValueChange = { onPreviewUpdate(it, previewM, previewS) }                )
                Text("Hr", fontWeight = FontWeight.Bold)
                NumberPicker(
                    value = previewM,
                    range = (0..59).toPersistentList(),
                    onValueChange = { onPreviewUpdate(previewH, it, previewS) }                )
                Text("Min", fontWeight = FontWeight.Bold)
                NumberPicker(
                    value = previewS,
                    range = (0..59).toPersistentList(),
                    onValueChange = { onPreviewUpdate(previewH, previewM, it) }                )
                Text("Sec", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    reducer.postAction(UpsertWorkoutAction.TimeSet(previewH, previewM, previewS))
                    reducer.postAction(UpsertWorkoutAction.SaveClicked)
                },
                enabled = controlsEnabled &&
                        state.error == null &&
                        state.name.isNotBlank() &&
                        (previewH + previewM + previewS) > 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text(primaryText) }

            TextButton(
                onClick = { reducer.postAction(UpsertWorkoutAction.Cancel) },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancel") }
        }
    }
}

@Composable
private fun MediaHeader(
    mediaItem: MediaItem?,
    imageUri: String?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        when {
            mediaItem != null -> {
                MediaItemPreview(
                    item = mediaItem,
                )
            }
            !imageUri.isNullOrBlank() -> {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(imageUri)
                        .decoderFactory { result, options, _ ->
                            if (android.os.Build.VERSION.SDK_INT >= 28) {
                                coil.decode.ImageDecoderDecoder(result.source, options)
                            } else {
                                coil.decode.GifDecoder(result.source, options)
                            }
                        }
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tap to choose a GIF",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AddWorkoutPreview_Loading() {
    val reducer = UpsertWorkoutReducer(InMemoryWorkoutRepository(), imageStore = WorkoutImageStore(LocalContext.current))
    UpsertWorkoutScreen(UpsertWorkoutState(workoutId = "123", viewDisplayMode = ViewDisplayMode.Loading), reducer)
}

@Preview
@Composable
fun AddWorkoutPreview() {
    val reducer = UpsertWorkoutReducer(InMemoryWorkoutRepository(), imageStore = WorkoutImageStore(LocalContext.current))
    UpsertWorkoutScreen(UpsertWorkoutState(viewDisplayMode = ViewDisplayMode.Empty), reducer)
}

@Preview(showBackground = true)
@Composable
private fun UpsertWorkoutEditPreview() {
    val dummyState = UpsertWorkoutState(
        viewDisplayMode = ViewDisplayMode.Content,
        workoutId = "dummy-id",
        name = "Morning Cardio",
        hours = 0,
        minutes = 45,
        seconds = 30,
        imageUri = "file:///android_asset/dummy_image.gif",
        error = null
    )

    val reducer = UpsertWorkoutReducer(
        InMemoryWorkoutRepository(),
        imageStore = WorkoutImageStore(LocalContext.current)
    )

    UpsertWorkoutScreen(
        state = dummyState,
        reducer = reducer
    )
}