package com.cortlandwalker.semaphore.data.local.room.helpers

import androidx.room.TypeConverter

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