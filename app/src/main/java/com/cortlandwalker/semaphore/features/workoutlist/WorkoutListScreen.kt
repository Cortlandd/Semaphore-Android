package com.cortlandwalker.semaphore.features.workoutlist

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cortlandwalker.semaphore.data.local.room.InMemoryWorkoutRepository
import com.cortlandwalker.semaphore.data.models.Workout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cortlandwalker.semaphore.core.helpers.ViewDisplayMode
import com.cortlandwalker.semaphore.features.workoutlist.WorkoutListAction.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    state: WorkoutListState,
    reducer: WorkoutListReducer,
    modifier: Modifier = Modifier
) {

    val listState = rememberLazyListState()
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingItem by remember { mutableStateOf<Workout?>(null) }
    var draggingItemInitialOffset by remember { mutableIntStateOf(0) } // 1. New state
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(state.workouts) {
        if (draggingItem != null && state.workouts.none { it.id == draggingItem?.id }) {
            draggingItem = null
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Semaphore") },
                actions = {
                    IconButton(onClick = { reducer.postAction(WorkoutListAction.TappedAddWorkout) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add workout")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.workouts.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { reducer.postAction(WorkoutListAction.PlayAllTapped) }
                ) { Icon(Icons.Default.PlayArrow, contentDescription = "Play all") }
            }
        }
    ) { inner ->
        val hasItems = state.workouts.isNotEmpty()

        when (state.displayMode) {
            ViewDisplayMode.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            ViewDisplayMode.Error -> {
                Text(state.error ?: "Error")
            }
            ViewDisplayMode.Content -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.workouts, key = { it.id }) { workout ->
                        val isExpanded = state.activeWorkoutId == workout.id
                        val isDragging = draggingItem?.id == workout.id

                        WorkoutRow(
                            workout = workout,
                            isExpanded = isExpanded,
                            onPlayClicked = { reducer.postAction(SinglePlayTapped(workout.id)) },
                            onClick = { reducer.postAction(WorkoutListAction.TappedWorkout(workout)) },
                            onLongPress = {},
                            activeProgress = if (isExpanded) state.activeWorkoutTimer else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .animateItem()
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    if (isDragging) {
                                        val currentInfo = listState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { it.key == workout.id }
                                        val currentOffset = currentInfo?.offset ?: draggingItemInitialOffset

                                        translationY = dragOffset + (draggingItemInitialOffset - currentOffset).toFloat()
                                        scaleX = 1.05f
                                        scaleY = 1.05f
                                        shadowElevation = 8f
                                    } else {
                                        translationY = 0f
                                        scaleX = 1f
                                        scaleY = 1f
                                        shadowElevation = 0f
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingItem = workout
                                            draggingItemIndex = state.workouts.indexOfFirst { it.id == workout.id }
                                            val info = listState.layoutInfo.visibleItemsInfo
                                                .firstOrNull { it.key == workout.id }
                                            draggingItemInitialOffset = info?.offset ?: 0
                                            dragOffset = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount.y

                                            val currentDraggingIndex = draggingItemIndex ?: return@detectDragGesturesAfterLongPress
                                            if (currentDraggingIndex !in state.workouts.indices) return@detectDragGesturesAfterLongPress

                                            val itemsInfo = listState.layoutInfo.visibleItemsInfo
                                            val currentItemInfo = itemsInfo.firstOrNull { it.key == draggingItem?.id }
                                                ?: return@detectDragGesturesAfterLongPress

                                            val currentItemCenter = draggingItemInitialOffset + (currentItemInfo.size / 2) + dragOffset

                                            val targetItem = itemsInfo.find { item ->
                                                val itemTop = item.offset
                                                val itemBottom = item.offset + item.size
                                                currentItemCenter > itemTop && currentItemCenter < itemBottom
                                            }

                                            if (targetItem != null && targetItem.key != draggingItem?.id) {
                                                val targetIndex = state.workouts.indexOfFirst { it.id == targetItem.key }

                                                if (targetIndex != -1 && targetIndex != currentDraggingIndex) {
                                                    reducer.postAction(UpdatePosition(workout, targetIndex))
                                                    draggingItemIndex = targetIndex
                                                }
                                            }

                                            // Simple implementation
                                            val viewportHeight = listState.layoutInfo.viewportSize.height
                                            if (currentItemCenter > viewportHeight - 100) {
                                                // trigger scroll down (requires coroutine scope)
                                                // scope.launch { listState.scrollBy(10f) }
                                            }
                                        },
                                        onDragEnd = {
                                            if (draggingItem != null) {
                                                val finalOrder = state.workouts.map { it.id }
                                                reducer.postAction(WorkoutListAction.ReorderCommit(finalOrder))
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
            ViewDisplayMode.Empty -> {
                EmptyWorkouts(
                    onAdd = { reducer.postAction(WorkoutListAction.TappedAddWorkout) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner)
                )
            }
        }
    }
}

@Composable
private fun EmptyWorkouts(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("No workouts yet", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tap the + in the top right or use the button below to add your first workout.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onAdd) { Text("Add workout") }
        }
    }
}

@Composable
fun DurationChip(workout: Workout) {
    val txt = "%02d:%02d:%02d".format(
        workout.hours.coerceAtLeast(0),
        workout.minutes.coerceAtLeast(0),
        workout.seconds.coerceAtLeast(0)
    )
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp
    ) {
        Text(
            text = txt,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview
@Composable
fun WorkoutListScreenEmpty_Preview() {
    val reducer = WorkoutListReducer(
        InMemoryWorkoutRepository()
    )
    WorkoutListScreen(
        state = WorkoutListState(displayMode = ViewDisplayMode.Content),
        reducer = reducer
    )
}
@Preview
@Composable
fun WorkoutListScreen_Preview() {
    val sample = listOf(
        Workout(
            id = "1",
            createdAt = 0,
            name = "Push Ups",
            imageUri = "",
            hours = 0,
            minutes = 5,
            seconds = 0,
            position = 0,
            orderId = 0
        ),
        Workout(id="2", createdAt=0, name="Plank",   imageUri="", hours=0, minutes=2, seconds=0, position=1, orderId=0),
    )
    val reducer = WorkoutListReducer(
        InMemoryWorkoutRepository(sample)
    )
    WorkoutListScreen(
        state = WorkoutListState(workouts = sample, displayMode = ViewDisplayMode.Content),
        reducer = reducer
    )
}