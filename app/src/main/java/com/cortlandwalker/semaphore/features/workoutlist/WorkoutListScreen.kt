package com.cortlandwalker.semaphore.features.workoutlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
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
import com.cortlandwalker.semaphore.features.workoutlist.WorkoutListAction.*
import com.cortlandwalker.semaphore.playback.WorkoutPlaybackController
import com.cortlandwalker.semaphore.playback.WorkoutPlaybackState
import com.cortlandwalker.semaphore.ui.components.BannerAd
import com.cortlandwalker.semaphore.ui.components.GridBackground
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    state: WorkoutListState,
    reducer: WorkoutListReducer,
    modifier: Modifier = Modifier
) {
    val purplePrimary = Color(0xFF6A5ACD)
    val listState = rememberLazyListState()
    val showRoutineOnly = state.isPlayingAll
    val routineTopSpacing by animateDpAsState(
        targetValue = if (showRoutineOnly) 8.dp else 16.dp,
        animationSpec = tween(durationMillis = 250),
        label = "routineTopSpacing"
    )

    // Drag state
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingItem by remember { mutableStateOf<Workout?>(null) }
    var draggingItemInitialOffset by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val latestWorkouts by rememberUpdatedState(state.workouts)

    LaunchedEffect(state.workouts) {
        if (draggingItem != null && state.workouts.none { it.id == draggingItem?.id }) {
            draggingItem = null
        }
    }

    LaunchedEffect(state.isPlayingAll, state.activeWorkoutId, state.workouts) {
        if (!state.isPlayingAll) return@LaunchedEffect

        val activeWorkoutId = state.activeWorkoutId ?: return@LaunchedEffect
        val activeIndex = state.workouts.indexOfFirst { it.id == activeWorkoutId }
        if (activeIndex == -1) return@LaunchedEffect

        listState.animateScrollToItem(index = activeIndex)
    }

    val isListEmpty = state.workouts.isEmpty() && state.displayMode != ViewDisplayMode.Loading

    // --- GLOBAL GRID BACKGROUND ---
    GridBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                CustomBottomBar(
                    isListEmpty = isListEmpty,
                    isPlayingAll = state.isPlayingAll,
                    isPlaybackPaused = state.isPlaybackPaused,
                    onMainAction = {
                        if (state.isPlayingAll) {
                            reducer.postAction(
                                if (state.isPlaybackPaused) {
                                    WorkoutListAction.ResumeTapped
                                } else {
                                    WorkoutListAction.PauseTapped
                                }
                            )
                        } else if (isListEmpty) {
                            reducer.postAction(WorkoutListAction.TappedAddWorkout)
                        } else {
                            reducer.postAction(WorkoutListAction.PlayAllTapped)
                        }
                    },
                    onSettings = { reducer.postAction(WorkoutListAction.TappedSettings) },
                    onTimer = {
                        if (state.isPlayingAll) {
                            reducer.postAction(WorkoutListAction.StopTapped)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (state.showBannerAd) {
                    Surface(color = Color.White) {
                        BannerAd(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .animateContentSize()
                        .clipToBounds()
                ) {
                    AnimatedVisibility(
                        visible = !showRoutineOnly,
                        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(250)),
                        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(250))
                    ) {
                        Column {
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

                            Spacer(Modifier.height(32.dp))
                        }
                    }

                    if (isListEmpty) {
                        EmptyStateContent(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        AnimatedVisibility(
                            visible = !showRoutineOnly,
                            enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
                            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(260))
                        ) {
                            Column {
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
                            }
                        }

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

                        Spacer(Modifier.height(routineTopSpacing))

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
                                        val latestWorkout by rememberUpdatedState(workout)

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
                                                            draggingItem = latestWorkout
                                                            draggingItemIndex =
                                                                latestWorkouts.indexOfFirst { it.id == latestWorkout.id }
                                                            val info =
                                                                listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == latestWorkout.id }
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

                                                            val targetIndex = targetIndexForDragReorder(
                                                                currentIndex = currentDraggingIndex,
                                                                draggingItemInitialOffset = draggingItemInitialOffset,
                                                                dragOffset = dragOffset,
                                                                currentItemSize = currentItemInfo.size,
                                                                orderedIds = latestWorkouts.map { it.id },
                                                                visibleItems = itemsInfo.mapNotNull { item ->
                                                                    (item.key as? String)?.let { id ->
                                                                        VisibleWorkoutItem(
                                                                            id = id,
                                                                            offset = item.offset,
                                                                            size = item.size
                                                                        )
                                                                    }
                                                                }
                                                            )

                                                            if (targetIndex != null && targetIndex != currentDraggingIndex) {
                                                                reducer.postAction(
                                                                    UpdatePosition(
                                                                        latestWorkout,
                                                                        targetIndex
                                                                    )
                                                                )
                                                                draggingItemIndex = targetIndex
                                                            }
                                                        },
                                                        onDragEnd = {
                                                            if (draggingItem != null) {
                                                                val finalOrder =
                                                                    latestWorkouts.map { it.id }
                                                                reducer.postAction(
                                                                    ReorderCommit(
                                                                        finalOrder
                                                                    )
                                                                )
                                                            }
                                                            draggingItem = null
                                                            draggingItemIndex = null
                                                            draggingItemInitialOffset = 0
                                                            dragOffset = 0f
                                                        },
                                                        onDragCancel = {
                                                            draggingItem = null
                                                            draggingItemIndex = null
                                                            draggingItemInitialOffset = 0
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
}

@Composable
fun CustomBottomBar(
    isListEmpty: Boolean,
    isPlayingAll: Boolean,
    isPlaybackPaused: Boolean,
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
                    AnimatedContent(
                        targetState = isPlayingAll,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)))
                                .togetherWith(fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(180)))
                                .using(SizeTransform(clip = false))
                        },
                        label = "timerTabState"
                    ) { playingAll ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (playingAll) Icons.Default.Stop else Icons.Default.Timer,
                                contentDescription = if (playingAll) "Stop routine" else "Timer",
                                tint = Color(0xFF6A5ACD)
                            )
                            Text(
                                if (playingAll) "Stop" else "Timer",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6A5ACD)
                            )
                        }
                    }
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
                AnimatedContent(
                    targetState = Triple(isPlayingAll, isPlaybackPaused, isListEmpty),
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180)))
                            .togetherWith(fadeOut(animationSpec = tween(120)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "mainActionState"
                ) { (playingAll, paused, listEmpty) ->
                    Icon(
                        imageVector = when {
                            playingAll && paused -> Icons.Default.PlayArrow
                            playingAll -> Icons.Default.Pause
                            listEmpty -> Icons.Default.Add
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = when {
                            playingAll && paused -> "Resume routine"
                            playingAll -> "Pause routine"
                            listEmpty -> "Add Workout"
                            else -> "Play All"
                        },
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Empty Workouts")
@Composable
fun WorkoutListScreenEmptyPreview() {
    val sample = emptyList<Workout>()
    val reducer = WorkoutListReducer(
        InMemoryWorkoutRepository(sample),
        PreviewWorkoutPlaybackController()
    )
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
    val reducer = WorkoutListReducer(
        InMemoryWorkoutRepository(sample),
        PreviewWorkoutPlaybackController()
    )
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

private class PreviewWorkoutPlaybackController : WorkoutPlaybackController {
    private val mutableState = MutableStateFlow(WorkoutPlaybackState())
    override val playbackState: StateFlow<WorkoutPlaybackState> = mutableState.asStateFlow()

    override fun startSingle(workout: Workout) = Unit

    override fun startAll(workouts: List<Workout>) = Unit

    override fun pause() = Unit

    override fun resume() = Unit

    override fun stop() = Unit
}
