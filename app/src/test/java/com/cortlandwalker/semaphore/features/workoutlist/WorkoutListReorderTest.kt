package com.cortlandwalker.semaphore.features.workoutlist

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WorkoutListReorderTest {

    @Test
    fun `dragging down does not reorder while only halfway into the next row`() {
        val targetIndex = targetIndexForDragReorder(
            currentIndex = 0,
            draggingItemInitialOffset = 0,
            dragOffset = 60f,
            currentItemSize = 100,
            orderedIds = listOf("a", "b", "c"),
            visibleItems = listOf(
                VisibleWorkoutItem("a", offset = 0, size = 100),
                VisibleWorkoutItem("b", offset = 116, size = 100),
                VisibleWorkoutItem("c", offset = 232, size = 100)
            )
        )

        assertThat(targetIndex).isNull()
    }

    @Test
    fun `dragging down reorders once the row crosses into the next slot`() {
        val targetIndex = targetIndexForDragReorder(
            currentIndex = 0,
            draggingItemInitialOffset = 0,
            dragOffset = 120f,
            currentItemSize = 100,
            orderedIds = listOf("a", "b", "c"),
            visibleItems = listOf(
                VisibleWorkoutItem("a", offset = 0, size = 100),
                VisibleWorkoutItem("b", offset = 116, size = 100),
                VisibleWorkoutItem("c", offset = 232, size = 100)
            )
        )

        assertThat(targetIndex).isEqualTo(1)
    }

    @Test
    fun `dragging up reorders once the row fully crosses into the previous slot`() {
        val targetIndex = targetIndexForDragReorder(
            currentIndex = 1,
            draggingItemInitialOffset = 116,
            dragOffset = -120f,
            currentItemSize = 100,
            orderedIds = listOf("a", "b", "c"),
            visibleItems = listOf(
                VisibleWorkoutItem("a", offset = 0, size = 100),
                VisibleWorkoutItem("b", offset = 116, size = 100),
                VisibleWorkoutItem("c", offset = 232, size = 100)
            )
        )

        assertThat(targetIndex).isEqualTo(0)
    }
}
