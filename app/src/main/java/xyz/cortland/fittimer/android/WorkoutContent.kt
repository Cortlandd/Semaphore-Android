package xyz.cortland.fittimer.android

import xyz.cortland.fittimer.android.model.Workout
import xyz.cortland.fittimer.android.model.WorkoutModel
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object WorkoutContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<Workout> = ArrayList()
    val WORKOUT_ITEMS: MutableList<WorkoutModel> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, Workout> = HashMap()
    val WORKOUT_ITEMS_MAP: MutableMap<String, WorkoutModel> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        ITEMS.add(Workout("1", "Mountain Climbers", "5"))
        ITEMS.add(Workout("2", "Push Ups", "45"))

        ITEM_MAP.put("1", Workout("1", "Mountain Climbers", "5"))
        ITEM_MAP.put("2", Workout("2", "Push Ups", "45"))
    }

    private fun addItem(item: Workout) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createDummyItem(position: Int): Workout {
        return Workout(
            position.toString(),
            "Item " + position,
            makeDetails(position)
        )
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }
}
