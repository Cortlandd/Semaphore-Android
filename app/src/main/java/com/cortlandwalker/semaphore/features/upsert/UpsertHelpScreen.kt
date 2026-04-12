package com.cortlandwalker.semaphore.features.upsert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cortlandwalker.semaphore.ui.components.GridBackground

@Composable
fun UpsertHelpScreen(
    onBack: () -> Unit
) {
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
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UpsertTopBar(
                            title = "Help",
                            onBack = onBack,
                            onHelp = {}
                        )
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
                        .height(220.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Workout Editor Help",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2D3142)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "This page uses the same visual components as the workout editor, but explains them in a simple reading order. That keeps the screen helpful visually while still staying clear for accessibility tools.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .offset(y = (-24).dp)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    HelpSection(
                        number = "1",
                        title = "Add Cover Media",
                        description = "This is the large media area near the top of the editor. Tap it to choose a GIF or other visual reference for the workout."
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            MediaSelectionArea(
                                mediaItem = null,
                                imageUri = null,
                                onTap = {}
                            )
                        }
                    }

                    HelpSection(
                        number = "2",
                        title = "Workout Name",
                        description = "This box is where you type the workout name. Semaphore uses this name in your routine list and during playback."
                    ) {
                        WorkoutNameInput(
                            name = "Workout Name",
                            speakNameAloud = false,
                            onNameChange = {},
                            onSpeechIconTap = {},
                            readOnly = true
                        )
                    }

                    HelpSection(
                        number = "3",
                        title = "Speaker Icon",
                        description = "The speaker icon inside the workout name box turns spoken workout names on or off for that specific workout. When it is on, Semaphore can say the workout name out loud when the timer starts."
                    ) {
                        WorkoutNameInput(
                            name = "Push Ups",
                            speakNameAloud = true,
                            onNameChange = {},
                            onSpeechIconTap = {},
                            readOnly = true
                        )
                    }

                    HelpSection(
                        number = "4",
                        title = "Duration",
                        description = "The duration area controls how long the workout lasts. The three pickers let you set hours, minutes, and seconds."
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DurationHeader()
                            TimePickerCard(
                                hours = 0,
                                minutes = 1,
                                seconds = 30,
                                onTimeChange = { _, _, _ -> }
                            )
                        }
                    }

                    HelpSection(
                        number = "5",
                        title = "Save Workout",
                        description = "The save button stores your changes. It creates a new workout when you are adding one, or updates the workout when you are editing."
                    ) {
                        SaveButtonFooter(
                            isSaving = false,
                            isEnabled = true,
                            onSave = {}
                        )
                    }

                    HelpSection(
                        number = "6",
                        title = "Navigation Buttons",
                        description = "The back button returns to your routine. The question mark button opens this help screen again any time you need a reminder."
                    ) {
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UpsertTopBar(
                                title = "New Workout",
                                onBack = {},
                                onHelp = {}
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Accessibility Notes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2D3142)
                            )
                            Text(
                                text = "This guide keeps the editor controls in a predictable order with plain-language explanations under each example. That makes it easier to follow visually and easier to move through with a screen reader.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun HelpSection(
    number: String,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = Color(0xFFEBE9F8),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = number,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF6A5ACD)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2D3142)
            )
            content()
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpsertHelpScreenPreview() {
    UpsertHelpScreen(onBack = {})
}
