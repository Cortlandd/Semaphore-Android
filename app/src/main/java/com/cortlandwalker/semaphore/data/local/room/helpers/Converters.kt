package com.cortlandwalker.semaphore.data.local.room.helpers

import androidx.room.TypeConverter

/**
 * # Room Converters
 *
 * Type converters used by Room to persist complex types.
 *
 * ## Example
 * ```kotlin
 * @TypeConverter fun fromList(v: List<String>): String = ...
 * ```
 */
class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.joinToString("||")

    /**
     * Executes toStringList.
     * @param data
     * @return List<String>
     */
    @TypeConverter
    fun toStringList(data: String?): List<String> =
        data?.takeIf { it.isNotEmpty() }?.split("||") ?: emptyList()
}