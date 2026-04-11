package com.cortlandwalker.semaphore.features.markdown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cortlandwalker.semaphore.ui.components.GridBackground
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkdownScreen(
    title: String,
    filename: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var markdownContent by remember { mutableStateOf("") }

    // Load file content from assets asynchronously
    LaunchedEffect(filename) {
        try {
            markdownContent = context.assets.open(filename)
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            markdownContent = if (filename == "PREVIEW_MODE") {
                "# Preview Content\nThis is how the markdown will look."
            } else {
                "# Error\nCould not load $filename"
            }
        }
    }

    val backgroundColor = Color(0xFFF8F8FA)

    GridBackground(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = backgroundColor
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onBack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2D3142)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .offset(y = (-24).dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            MarkdownText(
                                markdown = markdownContent,
                                style = TextStyle(
                                    color = Color(0xFF2D3142),
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkdownScreenPreview() {
    MarkdownScreen(
        title = "Licenses",
        filename = "PREVIEW_MODE",
        onBack = {}
    )
}
