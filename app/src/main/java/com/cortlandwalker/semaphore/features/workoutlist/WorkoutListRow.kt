package com.cortlandwalker.semaphore.features.workoutlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

// --- Design Tokens (Shared) ---
private val PurplePrimary = Color(0xFF6A5ACD)
private val InactiveBackgroundColor = Color.White
private val DurationPillColor = Color(0xFFF0F0F5)
private val PurplePillBackground = Color(0xFFEBE9F8)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WorkoutRow(
    workout: Workout,
    isExpanded: Boolean,
    onPlayClicked: (Workout) -> Unit,
    onClick: (Workout) -> Unit,
    activeProgress: String? = null,
    modifier: Modifier = Modifier
) {
    // Logic: Only fully expand visually if there is an image.
    val canExpandVisually = !workout.imageUri.isNullOrBlank()
    val showExpandedLayout = isExpanded && canExpandVisually

    // Height logic:
    // If expanding visually (has image + active) -> 380.dp
    // If collapsed (no image OR inactive) -> 100.dp
    val cardHeight by animateDpAsState(
        if (showExpandedLayout) 380.dp else 100.dp,
        label = "height",
        animationSpec = tween(durationMillis = 300)
    )

    val borderColor by animateColorAsState(
        if (isExpanded) PurplePrimary else Color.Transparent,
        label = "border",
        animationSpec = tween(durationMillis = 300)
    )
    val borderWidth by animateDpAsState(
        if (isExpanded) 2.dp else 0.dp,
        label = "width",
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = InactiveBackgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .border(borderWidth, borderColor, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick(workout) }
    ) {
        // Use AnimatedContent here to crossfade nicely between the big expanded view and the collapsed views
        AnimatedContent(
            targetState = showExpandedLayout,
            label = "layout_switch",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
            }
        ) { expanded ->
            if (expanded) {
                ExpandedWorkoutContent(
                    workout = workout,
                    activeProgress = activeProgress,
                    onStopClicked = { onPlayClicked(workout) }
                )
            } else {
                CollapsedWorkoutContent(
                    workout = workout,
                    isActive = isExpanded, // Timer is running
                    activeProgress = activeProgress,
                    onPlayPauseClicked = { onPlayClicked(workout) }
                )
            }
        }
    }
}

@Composable
private fun ExpandedWorkoutContent(
    workout: Workout,
    activeProgress: String?,
    onStopClicked: () -> Unit
) {
    val ctx = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Expanded Header Row
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
                        color = PurplePrimary
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
                color = PurplePillBackground,
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
                            color = PurplePrimary
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

            // Stop Button
            IconButton(
                onClick = onStopClicked,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DurationPillColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = PurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Hero Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(workout.imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Workout visual",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

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
                            .background(Color(0xFF00E676))
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
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CollapsedWorkoutContent(
    workout: Workout,
    isActive: Boolean,
    activeProgress: String?,
    onPlayPauseClicked: () -> Unit
) {
    // We use AnimatedContent to smoothly transition between the "Idle List Item" look
    // and the "Active Header" look.
    AnimatedContent(
        targetState = isActive,
        label = "collapsed_state_anim",
        transitionSpec = {
            if (targetState) {
                // Expanding to Active: Fade In + Slide Up
                (fadeIn(animationSpec = tween(300)) + slideInVertically { height -> height / 2 }) with
                        (fadeOut(animationSpec = tween(300)) + slideOutVertically { height -> -height / 2 })
            } else {
                // Returning to Inactive: Fade In + Slide Down
                (fadeIn(animationSpec = tween(300)) + slideInVertically { height -> -height / 2 }) with
                        (fadeOut(animationSpec = tween(300)) + slideOutVertically { height -> height / 2 })
            }.using(SizeTransform(clip = false))
        }
    ) { active ->
        if (active) {
            // --- ACTIVE STATE (Header Look) ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
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

                Spacer(Modifier.width(8.dp))

                // Active Timer Pill
                Surface(
                    color = PurplePillBackground,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeProgress ?: "00:00",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary
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

                Spacer(Modifier.width(8.dp))

                // Stop Button
                IconButton(
                    onClick = onPlayPauseClicked,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DurationPillColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // --- INACTIVE STATE (List Item Look) ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Drag Handle
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "Drag",
                    tint = Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.width(16.dp))

                // 2. Thumbnail (Or GIF Placeholder)
                WorkoutThumb(uri = workout.imageUri, size = 64)

                Spacer(Modifier.width(16.dp))

                // 3. Info Column
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

                    // Standard Duration Pill (Grey)
                    Surface(
                        color = DurationPillColor,
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

                // 4. Play Button
                IconButton(
                    onClick = onPlayPauseClicked,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PurplePillBackground)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = PurplePrimary
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

// --- Previews ---
@Preview(name = "Workout Row Idle", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun WorkoutRowCollapsedPreview() {
    val w = Workout(id = "1", createdAt = 0, name = "Warm Up", imageUri = "", hours = 0, minutes = 2, seconds = 0, position = 0, orderId = 0)
    Box(Modifier.padding(16.dp)) {
        WorkoutRow(workout = w, onPlayClicked = {}, onClick = {}, isExpanded = false)
    }
}

@Preview(name = "Workout Row Playing without Image", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun WorkoutRowActiveCollapsedPreview() {
    val w = Workout(id = "1", createdAt = 0, name = "Push Ups", imageUri = "", hours = 0, minutes = 0, seconds = 33, position = 0, orderId = 0)
    Box(Modifier.padding(16.dp)) {
        WorkoutRow(
            workout = w,
            onPlayClicked = {},
            onClick = {},
            isExpanded = true,
            activeProgress = "00:24"
        )
    }
}

@Preview(name = "Workout Row with Image", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun WorkoutRowWithImagePreview() {
    val w = Workout(id = "1", createdAt = 0, name = "Push Ups", imageUri = "https://example.com/image.gif", hours = 0, minutes = 0, seconds = 33, position = 0, orderId = 0)
    Box(Modifier.padding(16.dp)) {
        WorkoutRow(
            workout = w,
            onPlayClicked = {},
            onClick = {},
            isExpanded = false
        )
    }
}
