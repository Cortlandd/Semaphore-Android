package com.cortlandwalker.semaphore.features.workoutlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import com.cortlandwalker.semaphore.monetization.MonetizationUiState
import com.cortlandwalker.semaphore.features.workoutlist.WorkoutListAction.*
import com.cortlandwalker.semaphore.ui.components.BannerAd
import com.cortlandwalker.semaphore.ui.components.GridBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    state: WorkoutListState,
    reducer: WorkoutListReducer,
    monetizationState: MonetizationUiState = MonetizationUiState(),
    modifier: Modifier = Modifier
) {
    val purplePrimary = Color(0xFF6A5ACD)
    val listState = rememberLazyListState()

    // Drag state
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingItem by remember { mutableStateOf<Workout?>(null) }
    var draggingItemInitialOffset by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(state.workouts) {
        if (draggingItem != null && state.workouts.none { it.id == draggingItem?.id }) {
            draggingItem = null
        }
    }

    val isListEmpty = state.workouts.isEmpty() && state.displayMode != ViewDisplayMode.Loading

    // --- GLOBAL GRID BACKGROUND ---
    GridBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    if (monetizationState.canShowBannerAds) {
                        Surface(color = Color.White) {
                            BannerAd(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    CustomBottomBar(
                        isListEmpty = isListEmpty,
                        onMainAction = {
                            if (isListEmpty) {
                                reducer.postAction(WorkoutListAction.TappedAddWorkout)
                            } else {
                                reducer.postAction(WorkoutListAction.PlayAllTapped)
                            }
                        },
                        onSettings = { reducer.postAction(WorkoutListAction.TappedSettings) },
                        onTimer = {}
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // 1. Header (Always Visible)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Semaphore",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        )
                        // Hide subtitle in empty state to match design cleanliness
                        if (!isListEmpty) {
                            Text(
                                text = "Ready to work out?",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        }
                    }

                    IconButton(
                        onClick = { reducer.postAction(WorkoutListAction.TappedAddWorkout) },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = purplePrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (isListEmpty) {
                    // --- EMPTY STATE DESIGN ---
                    EmptyStateContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Total Time Card
                        Card(
                            modifier = Modifier.weight(1f).height(110.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = purplePrimary),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Box(Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "TOTAL TIME",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(Modifier.height(4.dp))

                                    val totalSeconds = state.workouts.sumOf {
                                        (it.hours * 3600) + (it.minutes * 60) + it.seconds
                                    }
                                    val h = totalSeconds / 3600
                                    val m = (totalSeconds % 3600) / 60
                                    val s = totalSeconds % 60

                                    val timeString = when {
                                        h > 0 -> "${h}h ${m}m"
                                        m > 0 && s > 0 -> "${m}m ${s}s"
                                        m > 0 -> "${m}m"
                                        else -> "${s}s"
                                    }
                                    val fontSize =
                                        if (timeString.length > 5) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall
                                    Text(
                                        text = timeString,
                                        style = fontSize.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Workouts Count Card
                        Card(
                            modifier = Modifier.weight(1f).height(110.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Box(Modifier.fillMaxSize().padding(20.dp)) {
                                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                                    Text(
                                        "WORKOUTS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${state.workouts.size}",
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.Black
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(32.dp)
                                        .graphicsLayer { rotationZ = -45f }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // 3. Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Routine",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 4. Workout List
                    when (state.displayMode) {
                        ViewDisplayMode.Loading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = purplePrimary)
                            }
                        }
                        ViewDisplayMode.Error -> {
                            Text(state.error ?: "Error")
                        }
                        ViewDisplayMode.Content, ViewDisplayMode.Empty -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                items(state.workouts, key = { it.id }) { workout ->
                                    val isExpanded = state.activeWorkoutId == workout.id
                                    val isDragging = draggingItem?.id == workout.id

                                    WorkoutRow(
                                        workout = workout,
                                        isExpanded = isExpanded,
                                        activeProgress = if (isExpanded) state.activeWorkoutTimer else null,
                                        onPlayClicked = { reducer.postAction(SinglePlayTapped(workout.id)) },
                                        onClick = { reducer.postAction(TappedWorkout(workout)) },
                                        modifier = Modifier
                                            .animateItem()
                                            .zIndex(if (isDragging) 1f else 0f)
                                            .graphicsLayer {
                                                if (isDragging) {
                                                    val currentInfo =
                                                        listState.layoutInfo.visibleItemsInfo
                                                            .firstOrNull { it.key == workout.id }
                                                    val currentOffset =
                                                        currentInfo?.offset ?: draggingItemInitialOffset
                                                    translationY =
                                                        dragOffset + (draggingItemInitialOffset - currentOffset).toFloat()
                                                    scaleX = 1.03f
                                                    scaleY = 1.03f
                                                    shadowElevation = 16f
                                                }
                                            }
                                            .pointerInput(Unit) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        draggingItem = workout
                                                        draggingItemIndex =
                                                            state.workouts.indexOfFirst { it.id == workout.id }
                                                        val info =
                                                            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == workout.id }
                                                        draggingItemInitialOffset = info?.offset ?: 0
                                                        dragOffset = 0f
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffset += dragAmount.y
                                                        val currentDraggingIndex =
                                                            draggingItemIndex
                                                                ?: return@detectDragGesturesAfterLongPress
                                                        if (currentDraggingIndex !in state.workouts.indices) return@detectDragGesturesAfterLongPress

                                                        val itemsInfo =
                                                            listState.layoutInfo.visibleItemsInfo
                                                        val currentItemInfo =
                                                            itemsInfo.firstOrNull { it.key == draggingItem?.id }
                                                                ?: return@detectDragGesturesAfterLongPress
                                                        val currentItemCenter =
                                                            draggingItemInitialOffset + (currentItemInfo.size / 2) + dragOffset

                                                        val targetItem = itemsInfo.find { item ->
                                                            val itemTop = item.offset
                                                            val itemBottom = item.offset + item.size
                                                            currentItemCenter > itemTop && currentItemCenter < itemBottom
                                                        }

                                                        if (targetItem != null && targetItem.key != draggingItem?.id) {
                                                            val targetIndex =
                                                                state.workouts.indexOfFirst { it.id == targetItem.key }
                                                            if (targetIndex != -1 && targetIndex != currentDraggingIndex) {
                                                                reducer.postAction(
                                                                    UpdatePosition(
                                                                        workout,
                                                                        targetIndex
                                                                    )
                                                                )
                                                                draggingItemIndex = targetIndex
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        if (draggingItem != null) {
                                                            val finalOrder =
                                                                state.workouts.map { it.id }
                                                            reducer.postAction(
                                                                ReorderCommit(
                                                                    finalOrder
                                                                )
                                                            )
                                                        }
                                                        draggingItem = null
                                                        draggingItemIndex = null
                                                        dragOffset = 0f
                                                    },
                                                    onDragCancel = {
                                                        draggingItem = null
                                                        draggingItemIndex = null
                                                        dragOffset = 0f
                                                    }
                                                )
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    isListEmpty: Boolean,
    onMainAction: () -> Unit,
    onSettings: () -> Unit,
    onTimer: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(80.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // White Bar Background
        Surface(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer Item
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onTimer() }) {
                    Icon(Icons.Default.Timer, contentDescription = "Timer", tint = Color(0xFF6A5ACD))
                    Text("Timer", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6A5ACD))
                }

                // Gap for the Play/Add Button
                Spacer(Modifier.width(48.dp))

                // Settings Item
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSettings() }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    Text("Settings", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        Surface(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(70.dp)
                .clickable { onMainAction() },
            shape = CircleShape,
            color = Color(0xFF6A5ACD),
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    // Switch icon if list is empty
                    imageVector = if (isListEmpty) Icons.Default.Add else Icons.Default.PlayArrow,
                    contentDescription = if (isListEmpty) "Add Workout" else "Play All",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Preview(name = "Empty Workouts")
@Composable
fun WorkoutListScreenEmptyPreview() {
    val sample = emptyList<Workout>()
    val reducer = WorkoutListReducer(InMemoryWorkoutRepository(sample))
    WorkoutListScreen(
        state = WorkoutListState(
            workouts = sample,
            displayMode = ViewDisplayMode.Empty,
            activeWorkoutId = "",
            activeWorkoutTimer = ""
        ),
        reducer = reducer
    )
}

@Preview
@Composable
fun WorkoutListScreenPreview() {
    val sample = listOf(
        Workout("1", 0, "Warm Up", "", 0, 2, 0, 0, 0),
        Workout("2", 0, "Push Ups", "", 0, 0, 33, 1, 0),
        Workout("3", 0, "High Knees", "", 0, 1, 0, 2, 0),
        Workout("4", 0, "Cool Down", "", 0, 5, 0, 3, 0),
    )
    val reducer = WorkoutListReducer(InMemoryWorkoutRepository(sample))
    WorkoutListScreen(
        state = WorkoutListState(
            workouts = sample,
            displayMode = ViewDisplayMode.Content,
            activeWorkoutId = "2",
            activeWorkoutTimer = "00:00:24"
        ),
        reducer = reducer
    )
}
