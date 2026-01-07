package com.cortlandwalker.semaphore.features.workoutlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cortlandwalker.semaphore.data.models.Workout

@Composable
fun WorkoutRow(
    workout: Workout,
    isExpanded: Boolean,
    onPlayClicked: (Workout) -> Unit,
    onClick: (Workout) -> Unit,
    activeProgress: String? = null,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    // Design Tokens
    val purplePrimary = Color(0xFF6A5ACD)
    val inactiveBackgroundColor = Color.White
    val durationPillColor = Color(0xFFF0F0F5) // Light grey for pill background
    val purplePillBackground = Color(0xFFEBE9F8) // Very light purple for expanded timer pill

    val borderColor by animateColorAsState(if (isExpanded) purplePrimary else Color.Transparent, label = "border")
    val borderWidth by animateDpAsState(if (isExpanded) 2.dp else 0.dp, label = "width")
    val cardHeight by animateDpAsState(if (isExpanded) 380.dp else 100.dp, label = "height")

    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = inactiveBackgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .border(borderWidth, borderColor, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick(workout) }
    ) {
        if (isExpanded) {
            // --- EXPANDED LAYOUT ---
            Column(modifier = Modifier.fillMaxSize()) {

                // Header Row (Title, Timer, Stop Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title & Subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = workout.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = purplePrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Current Interval",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    // Timer Pill
                    Surface(
                        color = purplePillBackground,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeProgress ?: "00:00",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = purplePrimary
                                )
                            )
                            Text(
                                text = " / ${formatHmsShort(workout.hours, workout.minutes, workout.seconds)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Stop Button (Square-ish circle)
                    IconButton(
                        onClick = { onPlayClicked(workout) }, // Acts as stop/pause in expanded mode
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(durationPillColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = purplePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Hero Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Fill remaining space
                        .background(Color.Black)
                ) {
                    if (!workout.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx)
                                .data(workout.imageUri)
                                .decoderFactory { result, options, _ ->
                                    if (android.os.Build.VERSION.SDK_INT >= 28) {
                                        coil.decode.ImageDecoderDecoder(result.source, options)
                                    } else {
                                        coil.decode.GifDecoder(result.source, options)
                                    }
                                }
                                .crossfade(true)
                                .build(),
                            contentDescription = "Workout visual",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder for expanded state
                        Box(
                            Modifier.fillMaxSize().background(Color(0xFFE8E8EE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // "Active" Badge overlay
                    Surface(
                        color = Color(0xFF222222).copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676)) // Bright Green dot
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // --- COLLAPSED LAYOUT ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag Handle
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "Drag",
                    tint = Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.width(16.dp))

                // Small Thumbnail
                WorkoutThumb(uri = workout.imageUri, size = 64)

                Spacer(Modifier.width(16.dp))

                // Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = workout.name.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(6.dp))

                    // Duration Pill
                    Surface(
                        color = durationPillColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = formatHmsShort(workout.hours, workout.minutes, workout.seconds),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Play Button
                IconButton(
                    onClick = { onPlayClicked(workout) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(purplePillBackground) // Light purple background
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = purplePrimary // Purple Icon
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutThumb(uri: String?, size: Int = 64) {
    val ctx = LocalContext.current
    val dim = size.dp
    val shape = CircleShape

    if (uri.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(dim)
                .clip(shape)
                .background(Color(0xFFF0F0F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "GIF",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.LightGray
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(uri)
                .decoderFactory { result, options, _ ->
                    if (android.os.Build.VERSION.SDK_INT >= 28) {
                        coil.decode.ImageDecoderDecoder(result.source, options)
                    } else {
                        coil.decode.GifDecoder(result.source, options)
                    }
                }
                .crossfade(true)
                .build(),
            contentDescription = "Workout image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(dim)
                .clip(shape)
        )
    }
}

private fun formatHmsShort(h: Int, m: Int, s: Int): String {
    return if (h > 0) {
        "%02d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun WorkoutRowPreview() {
    val w = Workout(id = "1", createdAt = 0, name = "Warm Up", imageUri = "", hours = 0, minutes = 2, seconds = 0, position = 0, orderId = 0)
    Box(Modifier.padding(16.dp)) {
        WorkoutRow(workout = w, onPlayClicked = {}, onClick = {}, isExpanded = false)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun WorkoutRowActivePreview() {
    val w = Workout(id = "1", createdAt = 0, name = "Push Ups", imageUri = "", hours = 0, minutes = 0, seconds = 33, position = 0, orderId = 0)
    Box(Modifier.padding(16.dp)) {
        WorkoutRow(workout = w, onPlayClicked = {}, onClick = {}, isExpanded = true, activeProgress = "00:24")
    }
}
