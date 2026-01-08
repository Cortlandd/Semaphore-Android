package com.cortlandwalker.semaphore.features.workoutlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyStateContent(modifier: Modifier = Modifier) {
    val purplePrimary = Color(0xFF6A5ACD)
    val accentPurple = Color(0xFFF3E5F5)
    val orangeBg = Color(0xFFFFF3E0)
    val orangeMain = Color(0xFFFF9800)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Background Ambient Blobs
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-40).dp, y = (-80).dp)
                .size(200.dp)
                .background(purplePrimary.copy(alpha = 0.05f), CircleShape)
                .blur(40.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 40.dp, y = 80.dp)
                .size(160.dp)
                .background(purplePrimary.copy(alpha = 0.08f), CircleShape)
                .blur(40.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Central Graphic
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Main White Card
                Surface(
                    modifier = Modifier
                        .size(160.dp)
                        .rotate(3f),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 12.dp,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = purplePrimary.copy(alpha = 0.9f),
                            modifier = Modifier
                                .size(80.dp)
                                .rotate(-3f)
                        )
                    }
                }

                // Floating "Bolt" Circle (Top Right)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(56.dp),
                    shape = CircleShape,
                    color = accentPurple,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = purplePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Floating "Timer" Rounded Square (Bottom Left)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-8).dp, y = 8.dp)
                        .rotate(12f)
                        .size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = orangeBg,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = orangeMain,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(-12f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // Text Content
            Text(
                text = "No Workouts yet",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = buildAnnotatedString {
                    append("Tap the ")
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = purplePrimary
                    )
                    ) {
                        append("+")
                    }
                    append(" in the top right or use the button below to add your first workout.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp),
                lineHeight = 24.sp
            )
        }
    }
}