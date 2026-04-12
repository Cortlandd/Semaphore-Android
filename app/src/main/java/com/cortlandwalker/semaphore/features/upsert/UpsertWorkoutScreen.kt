package com.cortlandwalker.semaphore.features.upsert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository
import com.cortlandwalker.semaphore.data.local.room.WorkoutImageStore
import com.klipy.klipy_ui.components.MediaItemPreview
import com.klipy.sdk.model.MediaItem
import com.seo4d696b75.compose.material3.picker.NumberPicker
import kotlinx.collections.immutable.toPersistentList

@Composable
fun UpsertWorkoutScreen(
    state: UpsertWorkoutState,
    reducer: UpsertWorkoutReducer
) {
    // Local state variables...
    var hours by remember { mutableIntStateOf(state.hours) }
    var minutes by remember { mutableIntStateOf(state.minutes) }
    var seconds by remember { mutableIntStateOf(state.seconds) }

    LaunchedEffect(state.hours, state.minutes, state.seconds, state.viewDisplayMode) {
        if (state.viewDisplayMode != ViewDisplayMode.Loading) {
            hours = state.hours
            minutes = state.minutes
            seconds = state.seconds
        }
    }

    val backgroundColor = Color(0xFFF8F8FA)
    val scrollState = rememberScrollState()
    var showSpeechDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            if (state.viewDisplayMode == ViewDisplayMode.Content || state.viewDisplayMode == ViewDisplayMode.Empty) {
                SaveButtonFooter(
                    isSaving = state.isSaving,
                    isEnabled = state.name.isNotBlank() && (hours + minutes + seconds) > 0,
                    onSave = {
                        reducer.postAction(UpsertWorkoutAction.TimeSet(hours, minutes, seconds))
                        reducer.postAction(UpsertWorkoutAction.SaveClicked)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    MediaSelectionArea(
                        mediaItem = state.selectedMediaItem,
                        imageUri = state.imageUri,
                        onTap = { reducer.postAction(UpsertWorkoutAction.GifTapped) }
                    )

                    Box(modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        UpsertTopBar(
                            title = if (state.isEdit) "Edit Workout" else "New Workout",
                            onBack = { reducer.postAction(UpsertWorkoutAction.Cancel) },
                            onHelp = { reducer.postAction(UpsertWorkoutAction.HelpTapped) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .offset(y = (-32).dp)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    when (state.viewDisplayMode) {
                        ViewDisplayMode.Loading -> {
                            Box(Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF6A5ACD))
                            }
                        }
                        ViewDisplayMode.Error -> {
                            Text("Error: ${state.error}", color = Color.Red)
                        }
                        ViewDisplayMode.Content, ViewDisplayMode.Empty -> {

                            WorkoutNameInput(
                                name = state.name,
                                speakNameAloud = state.speakNameAloud,
                                onNameChange = { reducer.postAction(UpsertWorkoutAction.NameChanged(it)) },
                                onSpeechIconTap = { showSpeechDialog = true }
                            )

                            Spacer(Modifier.height(32.dp))

                            DurationHeader()
                            Spacer(Modifier.height(16.dp))
                            TimePickerCard(
                                hours = hours,
                                minutes = minutes,
                                seconds = seconds,
                                onTimeChange = { h, m, s -> hours = h; minutes = m; seconds = s }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSpeechDialog) {
        AlertDialog(
            onDismissRequest = { showSpeechDialog = false },
            title = {
                Text(if (state.speakNameAloud) "Turn off workout speech?" else "Turn on workout speech?")
            },
            text = {
                Text("When this is on, everytime a workout begins it will use text to speech to say that specific workout name you gave")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSpeechDialog = false
                        reducer.postAction(
                            UpsertWorkoutAction.SpeakNameAloudChanged(!state.speakNameAloud)
                        )
                    }
                ) {
                    Text(if (state.speakNameAloud) "Turn Off" else "Turn On")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSpeechDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Components ---

@Composable
private fun UpsertTopBar(title: String, onBack: () -> Unit, onHelp: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(48.dp)
                .clickable { onBack() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
        }

        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = Color.White, // Or very light grey
            modifier = Modifier.height(40.dp),
            shadowElevation = 0.dp // Flat look in design
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(48.dp)
                .clickable { onHelp() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun MediaSelectionArea(
    mediaItem: MediaItem?,
    imageUri: String?,
    onTap: () -> Unit
) {
    val purplePrimary = Color(0xFF6A5ACD)
    val gridColor = Color.Gray.copy(alpha = 0.15f)
    val backgroundColor = Color(0xFFF2F2F7)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .drawGrid(color = gridColor, step = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFFF8F8FA)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = if (imageUri.isNullOrBlank()) purplePrimary else Color.Transparent,
                // Add the 'glow' shadow effect
                shadowElevation = if (imageUri.isNullOrBlank()) 10.dp else 0.dp,
                modifier = Modifier
                    .size(150.dp)
                    .clickable { onTap() },
            ) {
                if (mediaItem != null) {
                    Box(Modifier.fillMaxSize()) { MediaItemPreview(item = mediaItem) }
                } else if (!imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
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
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Media",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (imageUri.isNullOrBlank()) {
                Text(
                    text = "Add Cover Media",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2D3142)
                )
                Text(
                    text = "GIF",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "Tap to Change",
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun WorkoutNameInput(
    name: String,
    speakNameAloud: Boolean,
    onNameChange: (String) -> Unit,
    onSpeechIconTap: () -> Unit
) {
    TextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = { Text("Workout Name", color = Color.Gray.copy(alpha = 0.7f)) },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onSpeechIconTap) {
                Icon(
                    imageVector = if (speakNameAloud) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = if (speakNameAloud) "Disable workout speech" else "Enable workout speech",
                    tint = if (speakNameAloud) Color(0xFF6A5ACD) else Color.Gray
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
        shape = RoundedCornerShape(24.dp), // Fully rounded ends
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(2.dp, RoundedCornerShape(24.dp))
    )
}

@Composable
private fun DurationHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DURATION",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray
        )

        Surface(
            color = Color(0xFFEBE9F8), // Light purple
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Target",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF6A5ACD),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun TimePickerCard(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onTimeChange: (Int, Int, Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("HR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("MIN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("SEC", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            // Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hours
                NumberPicker(
                    value = hours,
                    range = (0..23).toPersistentList(),
                    onValueChange = { onTimeChange(it, minutes, seconds) },
                    labelStyle = MaterialTheme.typography.headlineMedium.copy(color = Color.Black),
                )

                // Divider dots
                Text(":", style = MaterialTheme.typography.headlineMedium, color = Color.LightGray)

                // Minutes
                NumberPicker(
                    value = minutes,
                    range = (0..59).toPersistentList(),
                    onValueChange = { onTimeChange(hours, it, seconds) },
                    labelStyle = MaterialTheme.typography.headlineMedium.copy(color = Color.Black),
                )

                // Divider dots
                Text(":", style = MaterialTheme.typography.headlineMedium, color = Color.LightGray)

                // Seconds
                NumberPicker(
                    value = seconds,
                    range = (0..59).toPersistentList(),
                    onValueChange = { onTimeChange(hours, minutes, it) },
                    labelStyle = MaterialTheme.typography.headlineMedium.copy(color = Color.Black),
                )
            }
        }
    }
}

@Composable
private fun SaveButtonFooter(isSaving: Boolean, isEnabled: Boolean, onSave: () -> Unit) {
    val purplePrimary = Color(0xFF6A5ACD)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = onSave,
            enabled = isEnabled && !isSaving,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = purplePrimary,
                disabledContainerColor = purplePrimary.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    8.dp,
                    RoundedCornerShape(24.dp),
                    spotColor = purplePrimary.copy(alpha = 0.5f)
                )
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Save Workout",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
    }
}

// --- Custom Grid Modifier ---
fun Modifier.drawGrid(
    color: Color,
    step: Dp,
    strokeWidth: Float = 1f // Thin lines
): Modifier = this.drawBehind {
    val stepPx = step.toPx()
    val width = size.width
    val height = size.height

    // Draw Vertical Lines
    var x = stepPx
    while (x < width) {
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = strokeWidth
        )
        x += stepPx
    }

    // Draw Horizontal Lines
    var y = stepPx
    while (y < height) {
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = strokeWidth
        )
        y += stepPx
    }
}


// --- Previews ---

@Preview(showBackground = true)
@Composable
private fun UpsertDesignPreview() {
    val dummyState = UpsertWorkoutState(
        viewDisplayMode = ViewDisplayMode.Content,
        workoutId = null,
        name = "",
        hours = 0,
        minutes = 29,
        seconds = 45,
        imageUri = null,
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
