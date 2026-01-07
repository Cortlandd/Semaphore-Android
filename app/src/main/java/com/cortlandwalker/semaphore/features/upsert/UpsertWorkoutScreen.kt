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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertWorkoutScreen(
    state: UpsertWorkoutState,
    reducer: UpsertWorkoutReducer
) {
    // local sheet + live preview
    var sheetOpen by remember { mutableStateOf(false) }
    var previewH by remember { mutableIntStateOf(state.hours) }
    var previewM by remember { mutableIntStateOf(state.minutes) }
    var previewS by remember { mutableIntStateOf(state.seconds) }

    // When loading finishes (or new state arrives) and the sheet isn't open,
    // keep the preview synced with state so header shows the loaded values.
    LaunchedEffect(state.hours, state.minutes, state.seconds, state.viewDisplayMode) {
        if (!sheetOpen && state.viewDisplayMode != ViewDisplayMode.Loading) {
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
                    sheetOpen = sheetOpen,
                    previewH = previewH,
                    previewM = previewM,
                    previewS = previewS,
                    onSheetToggle = { open -> sheetOpen = open },
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
    sheetOpen: Boolean,
    previewH: Int,
    previewM: Int,
    previewS: Int,
    onSheetToggle: (Boolean) -> Unit,
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

            // Big "00h 00m 00s"
            TimeHeader(
                h = if (sheetOpen) previewH else state.hours,
                m = if (sheetOpen) previewM else state.minutes,
                s = if (sheetOpen) previewS else state.seconds,
                onClick = {
                    if (controlsEnabled) {
                        onPreviewUpdate(state.hours, state.minutes, state.seconds)
                        onSheetToggle(true)
                    }
                }
            )

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

            Button(
                onClick = { reducer.postAction(UpsertWorkoutAction.SaveClicked) },
                enabled = controlsEnabled &&
                        state.error == null &&
                        state.name.isNotBlank() &&
                        (state.hours + state.minutes + state.seconds) > 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text(primaryText) }

            TextButton(
                onClick = { reducer.postAction(UpsertWorkoutAction.Cancel) },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancel") }
        }

        if (sheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { onSheetToggle(false) },
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                TimePadSheet(
                    initH = previewH,
                    initM = previewM,
                    initS = previewS,
                    onPreview = onPreviewUpdate,
                    onDone = { h, m, s ->
                        reducer.postAction(UpsertWorkoutAction.TimeSet(h, m, s))
                        onSheetToggle(false)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                )
            }
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

/** Big "00h 00m 00s" header */
@Composable
private fun TimeHeader(h: Int, m: Int, s: Int, onClick: () -> Unit) {
    val text = "%02dh %02dm %02ds".format(h.coerceAtLeast(0), m.coerceAtLeast(0), s.coerceAtLeast(0))
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        textAlign = TextAlign.Center
    )
}

/** Bottom sheet with numeric keypad; typing fills ss->mm->hh and previews via onPreview. */
@Composable
private fun TimePadSheet(
    initH: Int,
    initM: Int,
    initS: Int,
    onPreview: (Int, Int, Int) -> Unit,
    onDone: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var buffer by remember {
        val seed = ("%02d%02d%02d".format(initH, initM, initS)).trimStart('0')
        mutableStateOf(seed.takeLast(6))
    }
    val (h, m, s) = remember(buffer) { hmsFromBuffer(buffer) }

    LaunchedEffect(buffer) { onPreview(h, m, s) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            KeypadRow("1","2","3", onDigit = { d -> buffer = pushDigit(buffer, d) })
            KeypadRow("4","5","6", onDigit = { d -> buffer = pushDigit(buffer, d) })
            KeypadRow("7","8","9", onDigit = { d -> buffer = pushDigit(buffer, d) })
            KeypadBottomRow(
                onBackspace = { buffer = popDigit(buffer) },
                onZero = { buffer = pushDigit(buffer, "0") },
                onClear = { buffer = "" }
            )
        }
        Button(
            onClick = { onDone(h, m, s) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Done") }
    }
}

private fun hmsFromBuffer(buffer: String): Triple<Int, Int, Int> {
    val padded = buffer.padStart(6, '0')
    val h = padded.substring(0, 2).toIntOrNull() ?: 0
    val m = padded.substring(2, 4).toIntOrNull() ?: 0
    val s = padded.substring(4, 6).toIntOrNull() ?: 0
    return Triple(h, m, s)
}
private fun pushDigit(current: String, digit: String): String = (current + digit).takeLast(6)
private fun popDigit(current: String): String = if (current.isNotEmpty()) current.dropLast(1) else ""

@Composable
private fun KeypadRow(a: String, b: String, c: String, onDigit: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeypadButton(a, { onDigit(a) }); KeypadButton(b, { onDigit(b) }); KeypadButton(c, { onDigit(c) })
    }
}

@Composable
private fun KeypadButton(label: String, onClick: () -> Unit, size: Dp = 64.dp, modifier: Modifier = Modifier) {
    FilledTonalButton(onClick, shape = CircleShape, modifier = modifier.size(size), contentPadding = PaddingValues(0.dp)) {
        Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun KeypadBottomRow(onBackspace: () -> Unit, onZero: () -> Unit, onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onClear, modifier = Modifier.size(64.dp)) { Text("CLR", fontWeight = FontWeight.Bold) }
        KeypadButton("0", onClick = onZero)
        IconButton(onClick = onBackspace, modifier = Modifier.size(64.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Backspace")
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