package com.cortlandwalker.semaphore.features.markdown

import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class MarkdownScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun `MarkdownScreen preview state`() {
        paparazzi.snapshot {
            MarkdownScreen(
                title = "Licenses",
                filename = "PREVIEW_MODE",
                onBack = {}
            )
        }
    }
}
