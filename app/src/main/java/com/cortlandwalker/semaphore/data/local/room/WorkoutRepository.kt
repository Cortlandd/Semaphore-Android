package com.cortlandwalker.semaphore.data.local.room

import com.cortlandwalker.semaphore.data.models.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface WorkoutRepository {
    fun observeAllOrderedByPosition(): Flow<List<Workout>>
    suspend fun getById(id: String): Workout?
    suspend fun maxPosition(): Int
    suspend fun insert(workout: Workout)
    suspend fun deleteById(id: String)
    suspend fun updatePosition(id: String, position: Int)
    suspend fun update(workout: Workout)

    suspend fun updatePositions(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id -> updatePosition(id, index) }
    }
}

@Singleton
class RoomWorkoutRepository @Inject constructor(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override fun observeAllOrderedByPosition(): Flow<List<Workout>> =
        dao.observeAllOrderedByPosition()   // Room handles scheduler

    override suspend fun getById(id: String): Workout? = dao.getById(id)

    override suspend fun maxPosition(): Int = dao.maxPosition()

    override suspend fun insert(workout: Workout) { dao.insert(workout) }

    override suspend fun deleteById(id: String) { dao.delete(id) }

    override suspend fun update(workout: Workout) = dao.update(workout)

    override suspend fun updatePosition(id: String, position: Int) {
        dao.updatePosition(id, position)
    }
}

// For previews
class InMemoryWorkoutRepository(
    seed: List<Workout> = emptyList()
) : WorkoutRepository {
    private val state = MutableStateFlow(seed.sortedBy { it.position })

    override fun observeAllOrderedByPosition(): Flow<List<Workout>> = state.asStateFlow()
    override suspend fun getById(id: String): Workout? {
        TODO("Not yet implemented")
    }

    override suspend fun maxPosition(): Int = (state.value.maxOfOrNull { it.position } ?: -1)

    override suspend fun insert(workout: Workout) {
        state.value = (state.value + workout).sortedBy { it.position }
    }

    override suspend fun deleteById(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun updatePosition(id: String, position: Int) {
        val reordered = state.value.map {
            if (it.id == id) it.copy(position = position) else it
        }.sortedBy { it.position }
        state.value = reordered
    }

    override suspend fun update(workout: Workout) {
        TODO("Not yet implemented")
    }
}