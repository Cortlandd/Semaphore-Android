package xyz.cortland.semaphore.android.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import xyz.cortland.semaphore.android.model.ActivityModel

class ActivityDatabase(context: Context, factory: SQLiteDatabase.CursorFactory?): SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "activities.db"
        val TABLE_NAME = "Activities"
        val COLUMN_ID = "id"
        val COLUMN_POSITION = "position"
        val COLUMN_HOURS = "hours"
        val COLUMN_MINUTES = "minutes"
        val COLUMN_SECONDS = "seconds"
        val COLUMN_ACTIVITY = "activityName"
        val COLUMN_ACTIVITYIMAGE = "activityImage"
        val COLUMN_ACTIVITYSPEECH = "activitySpeech"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_POSITION + " INTEGER, " +
                COLUMN_HOURS + " INTEGER, " +
                COLUMN_MINUTES + " INTEGER, " +
                COLUMN_SECONDS + " INTEGER, " +
                COLUMN_ACTIVITY + " TEXT, " +
                COLUMN_ACTIVITYIMAGE + " TEXT, " +
                COLUMN_ACTIVITYSPEECH + " INTEGER" + ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * Method used to store an activity and it's index for later use and the ability to
     * move Activities.
     *
     * @param activityId: The ID of the Activity whose position is being saved.
     * @param position: The new index of the Activity that was moved/adjusted.
     */
    fun setPosition(activityId: Int?, position: Int?) {
        val values = ContentValues()
        values.put(COLUMN_POSITION, position)
        val db = this.writableDatabase
        db.update(TABLE_NAME, values,"$COLUMN_ID=$activityId", null)
        db.close()
    }

    fun addActivity(activityModel: ActivityModel) {
        val values = ContentValues()
        values.put(COLUMN_HOURS, activityModel.hours)
        values.put(COLUMN_MINUTES, activityModel.minutes)
        values.put(COLUMN_SECONDS, activityModel.seconds)
        values.put(COLUMN_ACTIVITY, activityModel.activityName)
        values.put(COLUMN_ACTIVITYIMAGE, activityModel.activityImage)
        values.put(COLUMN_ACTIVITYSPEECH, activityModel.activitySpeech)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllActivities(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_POSITION ASC", null)
    }

    fun removeActivity(id: Int) {
        val db = this.writableDatabase
        return db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID='$id'")
    }

    fun updateActivityImage(aid: Int) {
        val values = ContentValues()
        values.putNull(COLUMN_ACTIVITYIMAGE) // Set the removed activityModel to null
        val db = this.writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_ID=$aid", null)
        db.close()
    }

    fun allActivitiesList(): List<ActivityModel> {
        val activityModels: ArrayList<ActivityModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_POSITION ASC"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val aid = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                val hours = cursor.getInt(cursor.getColumnIndex(COLUMN_HOURS))
                val minutes = cursor.getInt(cursor.getColumnIndex(COLUMN_MINUTES))
                val seconds = cursor.getInt(cursor.getColumnIndex(COLUMN_SECONDS))
                val activityName = cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITY))
                val activityImage = cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITYIMAGE))
                val activitySpeech = cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVITYSPEECH))
                val activity = ActivityModel(hours = hours, minutes = minutes, seconds = seconds, activityName = activityName, activityImage = activityImage, activitySpeech = activitySpeech)
                activity.id = aid
                activityModels.add(activity)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return activityModels
    }

}