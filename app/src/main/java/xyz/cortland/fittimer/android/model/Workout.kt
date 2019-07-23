package xyz.cortland.fittimer.android.model

data class Workout(val id: String, val workout: String, val seconds: String) {
    override fun toString(): String = workout
}