package com.cortlandwalker.semaphore.features.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import com.cortlandwalker.semaphore.ui.components.GridBackground
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    state: AnalyticsState,
    reducer: AnalyticsReducer,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(0xFFF8F8FA)
    val scope = rememberCoroutineScope()

    GridBackground(
        modifier = modifier.fillMaxSize(),
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
                            .clickable { scope.launch { reducer.postAction(AnalyticsAction.TapBack) } }
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
            if (state.workouts.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    item {
                        AnalyticsHeader()
                    }
                    item {
                        WeeklyProgressCard(state)
                    }
                    if (state.topWorkouts.isNotEmpty()) {
                        item {
                            TopWorkoutsCard(state)
                        }
                    }
                    item {
                        SectionHeader("Individual Workouts", "See All")
                    }
                    items(state.workouts) { workout ->
                        IndividualWorkoutCard(workout)
                    }
                    item {
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        AnalyticsHeader()
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No workouts yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Add a workout to see your stats here", style = MaterialTheme.typography.bodyLarge, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            }
        }
    }
}

@Composable
private fun AnalyticsHeader() {
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
                text = "Analytics",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2D3142)
            )
        }
    }
}

@Composable
private fun WeeklyProgressCard(state: AnalyticsState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "SUMMARY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF6A5ACD)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Weekly Progress", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = when {
                            state.weeklyActiveDays == 0 -> "Start a workout to begin this week's streak."
                            state.weeklyProgress >= 1f -> "You hit your weekly goal. Keep the momentum going."
                            else -> "${state.weeklyActiveDays} of ${state.weeklyGoalDays} active days this week."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                CircularProgress(progress = state.weeklyProgress)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                SummaryStat("WORKOUTS", state.totalWorkouts.toString())
                SummaryStat("HOURS", "%.1f".format(state.totalHours))
                SummaryStat("STREAK", "${state.currentStreak}d")
            }
        }
    }
}

@Composable
private fun TopWorkoutsCard(state: AnalyticsState) {
    val maxValue = state.topWorkouts.maxOfOrNull { it.totalTimeSpentSeconds }?.coerceAtLeast(1L) ?: 1L

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "TOP WORKOUTS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF6A5ACD)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Most time invested",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2D3142)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "These workouts are ranked by total time spent, using the completions you have already logged.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.topWorkouts.forEachIndexed { index, workout ->
                    TopWorkoutRow(
                        rank = index + 1,
                        workout = workout,
                        progress = workout.totalTimeSpentSeconds / maxValue.toFloat()
                    )
                }
            }
        }
    }
}

@Composable
private fun CircularProgress(progress: Float) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFFEBE9F8),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 15f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF6A5ACD),
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = 15f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold)
            Text("goal", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8FA))
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun TopWorkoutRow(
    rank: Int,
    workout: WorkoutAnalyticsEntry,
    progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEBE9F8)
                ) {
                    Text(
                        text = rank.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF6A5ACD)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2D3142)
                    )
                    Text(
                        text = "${workout.completedCount} completions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = formatSeconds(workout.totalTimeSpentSeconds),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2D3142)
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = Color(0xFF6A5ACD),
            trackColor = Color(0xFFEBE9F8),
        )
    }
}

@Composable
private fun SectionHeader(title: String, actionText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        if (actionText.isNotBlank()) {
            Text(actionText, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6A5ACD), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IndividualWorkoutCard(workout: Workout) {
    val durationInSeconds = workout.hours * 3600 + workout.minutes * 60 + workout.seconds
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            if (!workout.displayImageUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(workout.displayImageUri)
                        .decoderFactory {
                                result, options, _ ->
                            if (android.os.Build.VERSION.SDK_INT >= 28) {
                                ImageDecoderDecoder(result.source, options)
                            } else {
                                GifDecoder(result.source, options)
                            }
                        }
                        .crossfade(true)
                        .build(),
                    contentDescription = workout.name,
                    modifier = Modifier.height(150.dp).fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.height(150.dp).fillMaxWidth().background(Color(0xFFF8F8FA))) {
                    Icon(imageVector = iconForDuration(durationInSeconds), contentDescription = "Intensity", tint = if (durationInSeconds >= 60) Color.Red else Color.Green, modifier = Modifier.size(48.dp).align(Alignment.Center))
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(workout.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = iconForDuration(durationInSeconds), contentDescription = "Intensity", tint = if (durationInSeconds > 30) Color.Red else Color.Green, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (durationInSeconds > 30) "High Intensity" else "Regular Routine", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    MetricItem("TIMES COMPLETED", "${workout.completedCount} sessions")
                    MetricItem("TOTAL TIME", formatSeconds(workout.totalTimeSpentSeconds))
                }
                Spacer(Modifier.height(12.dp))
                MetricItem("LAST PERFORMED", formatDate(workout.lastPerformedAt))
            }
        }
    }
}

private fun iconForDuration(durationInSeconds: Int): ImageVector {
    return if (durationInSeconds >= 60) Icons.Default.Bolt else Icons.Default.SelfImprovement
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
    }
}

private fun formatSeconds(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

private fun formatDate(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "Never"
    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true, name = "Analytics Screen with Data")
@Composable
private fun AnalyticsScreenPreview() {
    val sampleWorkouts = listOf(
        Workout(
            id = "1",
            createdAt = 0L,
            name = "Morning Stretch",
            imageUri = "https://static.klipy.com/ii/2711dd8a75a85be822d136ec94899b3f/40/01/vT7gcxyy.gif",
            hours = 0,
            minutes = 0,
            seconds = 25,
            position = 0,
            orderId = 0,
            completedCount = 15,
            totalTimeSpentSeconds = 4500,
            lastPerformedAt = System.currentTimeMillis() - 86400000, // 1 day ago
            currentStreak = 3
        ),
        Workout(
            id = "2",
            createdAt = 0L,
            name = "HIIT Blast",
            imageUri = null,
            hours = 0,
            minutes = 0,
            seconds = 45,
            position = 1,
            orderId = 0,
            completedCount = 32,
            totalTimeSpentSeconds = 57600,
            lastPerformedAt = System.currentTimeMillis() - 172800000, // 2 days ago
            currentStreak = 5
        )
    )

    val reducer = AnalyticsReducer(InMemoryWorkoutRepository(sampleWorkouts))
    val state = AnalyticsState(
        workouts = sampleWorkouts,
        totalWorkouts = 47,
        totalHours = 17.25f,
        currentStreak = 5,
        weeklyProgress = 0.75f
    )

    AnalyticsScreen(state = state, reducer = reducer)
}

@Preview(showBackground = true, name = "Analytics Screen Empty")
@Composable
private fun AnalyticsScreenEmptyPreview() {
    val reducer = AnalyticsReducer(InMemoryWorkoutRepository(emptyList()))
    val state = AnalyticsState(workouts = emptyList())

    AnalyticsScreen(state = state, reducer = reducer)
}
