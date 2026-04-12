package com.cortlandwalker.semaphore.core.helpers

/**
 * Represents the high-level loading state for screen content that can switch
 * between loading, error, populated content, or an intentionally empty view.
 */
enum class ViewDisplayMode {
    Loading, Error, Content, Empty
}
