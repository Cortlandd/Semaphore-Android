package xyz.cortland.semaphore.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.cortland.semaphore.android.model.ActivityEntity

@Database(entities = [ActivityEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun activityDao(): ActivityDAO

    /**
     *
     * Finally, we declare a companion object to get static
     * access to the method getAppDataBase which gives us a
     * singleton instance of the database.
     *
     */
    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null){
                synchronized(AppDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "SemaphoreDB").build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }
    }
}