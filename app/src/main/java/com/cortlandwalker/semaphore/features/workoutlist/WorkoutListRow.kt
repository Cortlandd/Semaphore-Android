package com.cortlandwalker.semaphore.features.workoutlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cortlandwalker.semaphore.data.models.Workout

@Composable
fun WorkoutRow(
    workout: Workout,
    onPlayClicked: (Workout) -> Unit,
    onClick: (Workout) -> Unit,
    onLongPress: (Workout) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick(workout) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: image (GIF/placeholder)
            WorkoutThumb(uri = workout.imageUri)

            Spacer(Modifier.width(12.dp))

            // Name (start) — ellipsized
            Text(
                text = workout.name.ifBlank { "Untitled workout" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.75f, fill = true)
            )

            // Center: time — force true visual center with a Box that spans remaining width
            Box(
                modifier = Modifier
                    .weight(1f, fill = true),
            ) {
                Text(
                    text = formatHms(workout.hours, workout.minutes, workout.seconds),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Right: circular play button
            IconButton(
                onClick = { onPlayClicked(workout) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun WorkoutThumb(uri: String?, size: Int = 56) {
    val ctx = LocalContext.current
    val dim = size.dp
    val shape = RoundedCornerShape(12.dp)

    if (uri.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(dim)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "GIF",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(ctx).data(uri).crossfade(true).build(),
            contentDescription = "Workout image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(dim)
                .clip(shape)
                .background(Color.Black.copy(alpha = 0.08f))
        )
    }
}

private fun formatHms(h: Int, m: Int, s: Int): String =
    "%02d:%02d:%02d".format(h.coerceAtLeast(0), m.coerceAtLeast(0), s.coerceAtLeast(0))

// ---------- Preview ----------
@Preview(showBackground = true)
@Composable
private fun WorkoutRowPreview() {
    val w = Workout(
        id = "1",
        createdAt = System.currentTimeMillis(),
        name = "Push-ups",
        imageUri = "", // placeholder
        hours = 0, minutes = 5, seconds = 0,
        position = 0,
        orderId = 0
    )
    WorkoutRow(
        workout = w,
        onPlayClicked = {},
        onClick = {},
        onLongPress = {}
    )
}