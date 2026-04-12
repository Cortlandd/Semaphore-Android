package com.cortlandwalker.semaphore.features.upsert

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cortlandwalker.semaphore.R
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
                                contentDescription = stringResource(R.string.back_content_description),
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
                        .height(260.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.upsert_help_title),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2D3142)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.upsert_help_intro),
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
                        title = stringResource(R.string.upsert_help_add_cover_title),
                        description = stringResource(R.string.upsert_help_add_cover_description)
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
                        title = stringResource(R.string.upsert_help_workout_name_title),
                        description = stringResource(R.string.upsert_help_workout_name_description)
                    ) {
                        WorkoutNameInput(
                            name = stringResource(R.string.workout_name_placeholder),
                            speakNameAloud = false,
                            onNameChange = {},
                            onSpeechIconTap = {},
                            readOnly = true
                        )
                    }

                    HelpSection(
                        number = "3",
                        title = stringResource(R.string.upsert_help_speaker_title),
                        description = stringResource(R.string.upsert_help_speaker_description)
                    ) {
                        WorkoutNameInput(
                            name = stringResource(R.string.upsert_help_example_push_ups),
                            speakNameAloud = true,
                            onNameChange = {},
                            onSpeechIconTap = {},
                            readOnly = true
                        )
                    }

                    HelpSection(
                        number = "4",
                        title = stringResource(R.string.upsert_help_duration_title),
                        description = stringResource(R.string.upsert_help_duration_description)
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
                        title = stringResource(R.string.upsert_help_save_title),
                        description = stringResource(R.string.upsert_help_save_description)
                    ) {
                        SaveButtonFooter(
                            isSaving = false,
                            isEnabled = true,
                            onSave = {}
                        )
                    }

                    HelpSection(
                        number = "6",
                        title = stringResource(R.string.upsert_help_navigation_title),
                        description = stringResource(R.string.upsert_help_navigation_description)
                    ) {
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UpsertTopBar(
                                title = stringResource(R.string.upsert_new_workout_title),
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
                                text = stringResource(R.string.upsert_help_accessibility_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2D3142)
                            )
                            Text(
                                text = stringResource(R.string.upsert_help_accessibility_description),
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
