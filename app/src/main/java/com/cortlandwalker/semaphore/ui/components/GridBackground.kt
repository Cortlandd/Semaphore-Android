package com.cortlandwalker.semaphore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GridBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF8F8FA), // Light grey default
    gridColor: Color = Color.Gray.copy(alpha = 0.15f),
    gridStep: Dp = 40.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .drawGrid(color = gridColor, step = gridStep)
    ) {
        // Gradient Fade at bottom (optional, can be passed as a param if logic varies)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            backgroundColor // Fade into solid background at bottom
                        )
                    )
                )
        )
        content()
    }
}

fun Modifier.drawGrid(
    color: Color,
    step: Dp,
    strokeWidth: Float = 1f
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