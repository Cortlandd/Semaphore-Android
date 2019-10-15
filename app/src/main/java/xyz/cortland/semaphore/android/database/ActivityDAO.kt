package xyz.cortland.semaphore.android.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import xyz.cortland.semaphore.android.model.ActivityEntity

@Dao
interface ActivityDAO {

    @Insert
    fun insertActivityEntity(activityEntity: ActivityEntity)

    @Update
    fun updateActivityEntity(activityEntity: ActivityEntity)

    @Delete
    fun deleteActivityEntity(activityEntity: ActivityEntity)

    @Query("SELECT * FROM ActivityEntity WHERE id = :id")
    fun getActivityEntityById(id: Int?): ActivityEntity

    @Query("SELECT * FROM ActivityEntity ORDER BY id")
    fun getActivityEntities(): LiveData<List<ActivityEntity>>

    @Query("UPDATE ActivityEntity SET position=:position WHERE id = :activityEntityId")
    fun updateActivityEntity(position: Int?, activityEntityId: Int?)
}