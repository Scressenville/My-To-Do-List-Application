package r411.project.todolistapplication.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import r411.project.todolistapplication.classes.TaskModelClass
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "TaskDataBase"
        private val TABLE_TASKS = "TaskTable"
        private val TABLE_CATEGORIES = "CategoryTable"
        private val TASK_ID = "task_id"
        private val TASK_CATEGORY = "category"
        private val TASK_DESCRIPTION = "description"
        private val TASK_DEADLINE = "deadline"
        private val TASK_STATUS = "status"
        private val CATEGORY_ID = "category_id"
        private val CATEGORY_NAME = "name"
        private val CATEGORY_ICON_UNICODE = "icon"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_CATEGORY_TABLE = (
            "CREATE TABLE $TABLE_CATEGORIES (" +
                "$CATEGORY_ID INTEGER PRIMARY KEY, " +
                "$CATEGORY_NAME TEXT UNIQUE," +
                "$CATEGORY_ICON_UNICODE INTEGER NOT NULL UNIQUE);"
        )

        val INSERT_CATEGORIES = (
            "INSERT INTO $TABLE_CATEGORIES ($CATEGORY_NAME, $CATEGORY_ICON_UNICODE) VALUES " +
                "('Courses', 0x1f6d2)," +
                "('Menage', 0x1f9f9)," +
                "('Travail', 0x1f4dd)," +
                "('Evenement', 0x1f38a)," +
                "('Cuisine', 0x1f370)," +
                "('Medical', 0x1fa7a)," +
                "('Famille', 0x1f468)," +
                "('Animaux', 0x1f407)," +
                "('Autres', 0x1f4d4);"
        )

        val CREATE_TASK_TABLE = (
            "CREATE TABLE $TABLE_TASKS (" +
                "$TASK_ID INTEGER PRIMARY KEY, " +
                "$TASK_CATEGORY INTEGER NOT NULL, " +
                "$TASK_DESCRIPTION TEXT NOT NULL, " +
                "$TASK_DEADLINE TEXT, " +
                "$TASK_STATUS INTEGER NOT NULL, " +
                "FOREIGN KEY ($TASK_CATEGORY) REFERENCES $TABLE_CATEGORIES ($CATEGORY_ID));"
            )

        db?.execSQL(CREATE_CATEGORY_TABLE)
        db?.execSQL(INSERT_CATEGORIES)
        db?.execSQL(CREATE_TASK_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    fun addTask(taskCategory: Int, taskDescription: String, taskDeadline: String?): Long{
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
        var status = 0
        if (taskDeadline != null) {
            if (TimeUnit.MINUTES.convert((dateFormat.parse(taskDeadline)!!.time - Date().time), TimeUnit.MILLISECONDS) < 0) {
                status = -1
            }
        }

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TASK_CATEGORY, taskCategory)
        contentValues.put(TASK_DESCRIPTION, taskDescription)
        contentValues.put(TASK_DEADLINE, taskDeadline)
        contentValues.put(TASK_STATUS, status)

        val success = db.insert(TABLE_TASKS, null, contentValues)

        db.close()

        return success
    }

    fun selectAllTasks():ArrayList<TaskModelClass>{
        val taskList:ArrayList<TaskModelClass> = ArrayList<TaskModelClass>()
        val selectQuery = "SELECT $TASK_ID, $CATEGORY_ICON_UNICODE, $TASK_DESCRIPTION, $TASK_DEADLINE, $TASK_STATUS " +
                            "FROM $TABLE_TASKS T INNER JOIN $TABLE_CATEGORIES C ON T.$TASK_CATEGORY = C.$CATEGORY_ID ORDER BY $TASK_ID"
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        }
        catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var taskId: Int
        var taskCategory: Int
        var taskDescription: String
        var taskDeadline: String?
        var taskStatus: Int
        if (cursor.moveToFirst()) {
            do {
                taskId = cursor.getInt(cursor.getColumnIndex(TASK_ID))
                taskCategory = cursor.getInt(cursor.getColumnIndex(CATEGORY_ICON_UNICODE))
                taskDescription = cursor.getString(cursor.getColumnIndex(TASK_DESCRIPTION))
                taskDeadline = cursor.getString(cursor.getColumnIndex(TASK_DEADLINE))
                taskStatus = cursor.getInt(cursor.getColumnIndex(TASK_STATUS))
                val task = TaskModelClass(taskId, taskCategory, taskDescription, taskDeadline, taskStatus)
                taskList.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return taskList
    }

    fun selectAllCategories():Array<String> {
        val categoryList = ArrayList<String>()
        val selectQuery =
            "SELECT $CATEGORY_NAME, $CATEGORY_ICON_UNICODE FROM $TABLE_CATEGORIES ORDER BY $CATEGORY_ID"

        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return arrayOf<String>()
        }
        var categoryName: String
        var categoryUnicode: Int
        if (cursor.moveToFirst()) {
            do {
                categoryName = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME))
                categoryUnicode = cursor.getInt(cursor.getColumnIndex(CATEGORY_ICON_UNICODE))
                val category = categoryName + " " + String(Character.toChars(categoryUnicode))
                categoryList.add(category)
            } while (cursor.moveToNext())
        }
        val categoryArray: Array<String> = categoryList.toTypedArray()
        cursor.close()
        return categoryArray
    }

    fun selectTaskFromId(id: Int): TaskModelClass?{
        val selectQuery = "SELECT $TASK_ID, $CATEGORY_ICON_UNICODE, $TASK_DESCRIPTION, $TASK_DEADLINE, $TASK_STATUS " +
                "FROM $TABLE_TASKS T INNER JOIN $TABLE_CATEGORIES C ON T.$TASK_CATEGORY = C.$CATEGORY_ID " +
                "WHERE $TASK_ID = $id"
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        }
        catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return null
        }
        cursor.moveToFirst()

        val taskId: Int = cursor.getInt(cursor.getColumnIndex(TASK_ID))
        val taskCategory: Int = cursor.getInt(cursor.getColumnIndex(CATEGORY_ICON_UNICODE))
        val taskDescription: String = cursor.getString(cursor.getColumnIndex(TASK_DESCRIPTION))
        val taskDeadline: String? = cursor.getString(cursor.getColumnIndex(TASK_DEADLINE))
        val taskStatus: Int = cursor.getInt(cursor.getColumnIndex(TASK_STATUS))

        val task = TaskModelClass(taskId, taskCategory, taskDescription, taskDeadline, taskStatus)
        cursor.close()
        return task
    }

    fun deleteTask(deleteId: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TASK_ID, deleteId)

        val success = db.delete(TABLE_TASKS, "$TASK_ID=$deleteId",null)

        db.close()
        return success
    }

    fun changeStatus(taskId: Int, status: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TASK_STATUS, status)

        val success = db.update(TABLE_TASKS, contentValues, "$TASK_ID=$taskId", null)
        db.close()

        return success
    }

    fun getCategoryIdByEmoji(emojiCode: Int): Int {
        val selectQuery =
            "SELECT $CATEGORY_ID, $CATEGORY_ICON_UNICODE FROM $TABLE_CATEGORIES"

        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return -1
        }
        if (cursor.moveToFirst()) {
            do {
                val icon = cursor.getInt(cursor.getColumnIndex(CATEGORY_ICON_UNICODE))
                if ( icon == emojiCode) return cursor.getInt(cursor.getColumnIndex(CATEGORY_ID))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return -1
    }

    fun modifyTask(taskId: Int, category: Int, description: String, deadline: String?): Int {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
        val status: Int =
            if (TimeUnit.MINUTES.convert((dateFormat.parse(deadline!!)!!.time - Date().time), TimeUnit.MILLISECONDS) > 0) {
                0
            } else -1

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TASK_CATEGORY, category)
        contentValues.put(TASK_DESCRIPTION, description)
        contentValues.put(TASK_DEADLINE, deadline)
        contentValues.put(TASK_STATUS, status)

        val success = db.update(TABLE_TASKS, contentValues, "$TASK_ID=$taskId", null)
        db.close()

        return success
    }

    fun selectTasksWithStatus(statusFilter: Int): ArrayList<TaskModelClass> {
        val taskList:ArrayList<TaskModelClass> = ArrayList<TaskModelClass>()
        val selectQuery = "SELECT $TASK_ID, $CATEGORY_ICON_UNICODE, $TASK_DESCRIPTION, $TASK_DEADLINE, $TASK_STATUS " +
                "FROM $TABLE_TASKS T INNER JOIN $TABLE_CATEGORIES C ON T.$TASK_CATEGORY = C.$CATEGORY_ID " +
                "WHERE $TASK_STATUS=$statusFilter ORDER BY $TASK_ID"
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(selectQuery, null)
        }
        catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var taskId: Int
        var taskCategory: Int
        var taskDescription: String
        var taskDeadline: String?
        var taskStatus: Int
        if (cursor.moveToFirst()) {
            do {
                taskId = cursor.getInt(cursor.getColumnIndex(TASK_ID))
                taskCategory = cursor.getInt(cursor.getColumnIndex(CATEGORY_ICON_UNICODE))
                taskDescription = cursor.getString(cursor.getColumnIndex(TASK_DESCRIPTION))
                taskDeadline = cursor.getString(cursor.getColumnIndex(TASK_DEADLINE))
                taskStatus = cursor.getInt(cursor.getColumnIndex(TASK_STATUS))
                val task = TaskModelClass(taskId, taskCategory, taskDescription, taskDeadline, taskStatus)
                taskList.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return taskList
    }
}