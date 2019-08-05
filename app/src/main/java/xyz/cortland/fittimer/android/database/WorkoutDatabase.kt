package xyz.cortland.fittimer.android.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import xyz.cortland.fittimer.android.model.WorkoutModel

class WorkoutDatabase(context: Context, factory: SQLiteDatabase.CursorFactory?): SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {


    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "workouts.db"
        val TABLE_NAME = "workouts"
        val COLUMN_ID = "id"
        val COLUMN_SECONDS = "seconds"
        val COLUMN_WORKOUT = "workout"
        val COLUMN_WORKOUTIMAGE = "workoutImage"
        val COLUMN_WORKOUTSPEECH = "workoutSpeech"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SECONDS + " INTEGER, " +
                COLUMN_WORKOUT + " TEXT, " +
                COLUMN_WORKOUTIMAGE + " TEXT, " +
                COLUMN_WORKOUTSPEECH + " INTEGER" + ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addWorkout(workoutModel: WorkoutModel) {
        val values = ContentValues()
        values.put(COLUMN_SECONDS, workoutModel.seconds)
        values.put(COLUMN_WORKOUT, workoutModel.workoutName)
        values.put(COLUMN_WORKOUTIMAGE, workoutModel.workoutImage)
        values.put(COLUMN_WORKOUTSPEECH, workoutModel.workoutSpeech)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllWorkouts(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun removeWorkout(id: Int) {
        val db = this.writableDatabase
        return db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID='$id'")
    }

    fun updateWorkoutImage(wid: Int) {
        val values = ContentValues()
        values.putNull(COLUMN_WORKOUTIMAGE) // Set the removed workout to null
        val db = this.writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_ID=$wid", null)
        db.close()
    }

    fun getWorkoutById(id: Int): WorkoutModel {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT FROM $TABLE_NAME WHERE $COLUMN_ID='$id'", null)
        val workout = WorkoutModel()
        if (cursor.moveToFirst()) {
            do {
                workout.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                workout.seconds = cursor.getInt(cursor.getColumnIndex(COLUMN_SECONDS))
                workout.workoutName = cursor.getString(cursor.getColumnIndex(COLUMN_WORKOUT))
                workout.workoutImage = cursor.getString(cursor.getColumnIndex(COLUMN_WORKOUTIMAGE))
                workout.workoutSpeech = cursor.getInt(cursor.getColumnIndex(COLUMN_WORKOUTSPEECH))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return workout
    }

    fun allWorkoutsList(): List<WorkoutModel> {
        val workouts: ArrayList<WorkoutModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val seconds = cursor.getInt(cursor.getColumnIndex(COLUMN_SECONDS))
                val workoutName = cursor.getString(cursor.getColumnIndex(COLUMN_WORKOUT))
                val workoutImage = cursor.getString(cursor.getColumnIndex(COLUMN_WORKOUTIMAGE))
                val workoutSpeech = cursor.getInt(cursor.getColumnIndex(COLUMN_WORKOUTSPEECH))
                val workout: WorkoutModel = WorkoutModel(seconds = seconds, workoutName = workoutName, workoutImage = workoutImage, workoutSpeech = workoutSpeech)
                workouts.add(workout)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return workouts
    }

}