package com.cortlandwalker.semaphore.features.workoutlist

internal data class VisibleWorkoutItem(
    val id: String,
    val offset: Int,
    val size: Int
)

internal fun targetIndexForDragReorder(
    currentIndex: Int,
    draggingItemInitialOffset: Int,
    dragOffset: Float,
    currentItemSize: Int,
    orderedIds: List<String>,
    visibleItems: List<VisibleWorkoutItem>
): Int? {
    if (currentIndex !in orderedIds.indices || dragOffset == 0f) return null

    val movingDown = dragOffset > 0f
    val adjacentIndex = when {
        movingDown && currentIndex < orderedIds.lastIndex -> currentIndex + 1
        !movingDown && currentIndex > 0 -> currentIndex - 1
        else -> return null
    }

    val adjacentItem = visibleItems.firstOrNull { it.id == orderedIds[adjacentIndex] } ?: return null
    val draggedTop = draggingItemInitialOffset + dragOffset
    val draggedBottom = draggedTop + currentItemSize

    val crossedIntoAdjacentSlot = if (movingDown) {
        draggedTop >= adjacentItem.offset
    } else {
        draggedBottom <= adjacentItem.offset + adjacentItem.size
    }

    return adjacentIndex.takeIf { crossedIntoAdjacentSlot }
}
