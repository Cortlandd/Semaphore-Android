package xyz.cortland.fittimer.android.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import xyz.cortland.fittimer.android.model.ActivityModel

class ActivityDatabase(context: Context, factory: SQLiteDatabase.CursorFactory?): SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "activities.db"
        val TABLE_NAME = "Activities"
        val COLUMN_ID = "id"
        val COLUMN_HOURS = "hours"
        val COLUMN_MINUTES = "minutes"
        val COLUMN_SECONDS = "seconds"
        val COLUMN_ACTIVITY = "activityModel"
        val COLUMN_ACTIVITYIMAGE = "activityImage"
        val COLUMN_ACTIVITYSPEECH = "activitySpeech"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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

    fun addActivity(activityModelModel: ActivityModel) {
        val values = ContentValues()
        values.put(COLUMN_HOURS, activityModelModel.hours)
        values.put(COLUMN_MINUTES, activityModelModel.minutes)
        values.put(COLUMN_SECONDS, activityModelModel.seconds)
        values.put(COLUMN_ACTIVITY, activityModelModel.activityName)
        values.put(COLUMN_ACTIVITYIMAGE, activityModelModel.activityImage)
        values.put(COLUMN_ACTIVITYSPEECH, activityModelModel.activitySpeech)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllActivities(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun removeActivity(id: Int) {
        val db = this.writableDatabase
        return db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID='$id'")
    }

    fun updateActivityImage(wid: Int) {
        val values = ContentValues()
        values.putNull(COLUMN_ACTIVITYIMAGE) // Set the removed activityModel to null
        val db = this.writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_ID=$wid", null)
        db.close()
    }

    fun getActivityById(id: Int): ActivityModel {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT FROM $TABLE_NAME WHERE $COLUMN_ID='$id'", null)
        val activityModel = ActivityModel()
        if (cursor.moveToFirst()) {
            do {
                activityModel.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                activityModel.hours = cursor.getInt(cursor.getColumnIndex(COLUMN_HOURS))
                activityModel.minutes = cursor.getInt(cursor.getColumnIndex(COLUMN_MINUTES))
                activityModel.seconds = cursor.getInt(cursor.getColumnIndex(COLUMN_SECONDS))
                activityModel.activityName = cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITY))
                activityModel.activityImage = cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITYIMAGE))
                activityModel.activitySpeech = cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVITYSPEECH))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return activityModel
    }

    fun allActivitiesList(): List<ActivityModel> {
        val activityModels: ArrayList<ActivityModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val hours = cursor.getInt(cursor.getColumnIndex(COLUMN_HOURS))
                val minutes = cursor.getInt(cursor.getColumnIndex(COLUMN_MINUTES))
                val seconds = cursor.getInt(cursor.getColumnIndex(COLUMN_SECONDS))
                val activityName = cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITY))
                val activityImage = cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITYIMAGE))
                val activitySpeech = cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVITYSPEECH))
                val activityModel: ActivityModel = ActivityModel(hours = hours, minutes = minutes, seconds = seconds, activityName = activityName, activityImage = activityImage, activitySpeech = activitySpeech)
                activityModels.add(activityModel)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return activityModels
    }

}