package com.cortlandwalker.semaphore.features.upsert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertWorkoutScreen(
    state: UpsertWorkoutState,
    reducer: UpsertWorkoutReducer
) {
    // local sheet + live preview (does not mutate state until Done)
    var sheetOpen by remember { mutableStateOf(false) }
    var previewH by remember { mutableIntStateOf(state.hours) }
    var previewM by remember { mutableIntStateOf(state.minutes) }
    var previewS by remember { mutableIntStateOf(state.seconds) }

    // When loading finishes (or new state arrives) and the sheet isn't open,
    // keep the preview synced with state so header shows the loaded values.
    LaunchedEffect(state.hours, state.minutes, state.seconds, state.isLoading) {
        if (!sheetOpen && !state.isLoading) {
            previewH = state.hours
            previewM = state.minutes
            previewS = state.seconds
        }
    }

    val title = if (state.isEdit) "Edit Workout" else "Add Workout"
    val primaryText = if (state.isEdit) "Update" else "Save"
    val controlsEnabled = !state.isSaving && !state.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { reducer.postAction(UpsertWorkoutAction.Cancel) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->

        // LOADING STATE: show spinner before assigning data to fields
        if (state.isEdit && state.isLoading) {
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
            return@Scaffold
        }

        // CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
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
                        previewH = state.hours
                        previewM = state.minutes
                        previewS = state.seconds
                        sheetOpen = true
                    }
                }
            )

            // Tap to choose GIF (Fragment handles effect)
            MediaHeader(
                uri = state.imageUri,
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
                onDismissRequest = { sheetOpen = false },
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                TimePadSheet(
                    initH = previewH,
                    initM = previewM,
                    initS = previewS,
                    onPreview = { h, m, s ->
                        previewH = h; previewM = m; previewS = s
                    },
                    onDone = { h, m, s ->
                        reducer.postAction(UpsertWorkoutAction.TimeSet(h, m, s))
                        sheetOpen = false
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
    uri: String?,
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
        if (uri.isNullOrBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text("Tap to choose a GIF", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(uri).crossfade(true).build(),
                contentDescription = "Selected GIF",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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
    // Start buffer from init values (HHMMSS). We keep it internal to the sheet.
    var buffer by remember {
        val seed = ("%02d%02d%02d".format(initH, initM, initS)).trimStart('0')
        mutableStateOf(seed.takeLast(6))
    }

    val (h, m, s) = remember(buffer) { hmsFromBuffer(buffer) }

    // Push live preview up to the header while the sheet is open
    LaunchedEffect(buffer) {
        onPreview(h, m, s)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
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

@Composable
private fun KeypadRow(
    a: String, b: String, c: String,
    onDigit: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeypadButton(
            label = a,
            onClick = { onDigit(a) }
        )
        KeypadButton(
            label = b,
            onClick = { onDigit(b) }
        )
        KeypadButton(
            label = c,
            onClick = { onDigit(c) }
        )
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    // Filled-tonal circle; change to ButtonDefaults.buttonColors(...) if you want solid color
    FilledTonalButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier.size(size),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Optional: matching circular buttons for the bottom row (⌫, 0, CLR). */
@Composable
private fun KeypadBottomRow(
    onBackspace: () -> Unit,
    onZero: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = onBackspace,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            content = { Icon(Icons.Default.ArrowBack, contentDescription = "Delete") }
        )
        KeypadButton(label = "0", onClick = onZero)
        KeypadButton(label = "CLR", onClick = onClear)
    }
}

@Composable
private fun FilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        shape = shape,
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        content = { Row(Modifier.size(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, content = content) }
    )
}

/* --- helpers --- */

private fun pushDigit(current: String, d: String): String {
    // cap at 6 digits (HHMMSS -> up to 99h)
    val next = (current + d).trimStart('0')
    val capped = if (next.isEmpty()) "0" else next
    return capped.takeLast(6)
}

private fun popDigit(current: String): String {
    val next = if (current.isEmpty()) "" else current.dropLast(1)
    return next
}

private fun hmsFromBuffer(buf: String): Triple<Int, Int, Int> {
    if (buf.isEmpty()) return Triple(0, 0, 0)
    val digits = buf.takeLast(6)
    val secStr = digits.takeLast(2).padStart(2, '0')
    val minStr = digits.dropLast(2).takeLast(2).padStart(2, '0')
    val hourStr = digits.dropLast(4)
    val h = hourStr.ifEmpty { "0" }.toInt()
    val m = minStr.toInt().coerceAtMost(59)
    val s = secStr.toInt().coerceAtMost(59)
    return Triple(h, m, s)
}

@Preview
@Composable
fun AddWorkoutPreview_Loading() {
    val reducer = UpsertWorkoutReducer(InMemoryWorkoutRepository())
    UpsertWorkoutScreen(UpsertWorkoutState(workoutId = "123", isLoading = true), reducer)
}

@Preview
@Composable
fun AddWorkoutPreview() {
    val reducer = UpsertWorkoutReducer(InMemoryWorkoutRepository())
    UpsertWorkoutScreen(UpsertWorkoutState(), reducer)
}