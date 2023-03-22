package r411.project.todolistapplication.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.security.identity.AccessControlProfileId
import r411.project.todolistapplication.classes.TaskModelClass

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "TaskDataBase"
        private val TABLE_TASKS = "TaskTable"
        private val TABLE_CATEGORIES = "CategoryTable"
        private val TASK_ID = "id"
        private val TASK_CATEGORY = "category"
        private val TASK_DESCRIPTION = "description"
        private val TASK_DEADLINE = "deadline"
        private val TASK_STATUS = "status"
        private val CATEGORY_NAME = "name"
        private val CATEGORY_ICON_UNICODE = "icon"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_CATEGORY_TABLE = (
            "CREATE TABLE $TABLE_CATEGORIES (" +
                "$CATEGORY_NAME TEXT PRIMARY KEY," +
                "$CATEGORY_ICON_UNICODE INTEGER NOT NULL);"
        )

        val INSERT_CATEGORIES = (
            "INSERT INTO $TABLE_CATEGORIES VALUES " +
                "('Courses', '0x1f6d2')," +
                "('Menage', '0x1f9f9')," +
                "('Travail', '0x270f')," +
                "('Evenement', '0x1f38a')," +
                "('Cuisine', '0x1f370')," +
                "('Medical', '0x1fa7a')," +
                "('Famille', '0x1f468')," +
                "('Animaux', '0x1f407')," +
                "('Autres', '0x1f4d4');"
        )

        val CREATE_TASK_TABLE = (
            "CREATE TABLE $TABLE_TASKS (" +
                "$TASK_ID INTEGER PRIMARY KEY, " +
                "$TASK_CATEGORY TEXT NOT NULL, " +
                "$TASK_DESCRIPTION TEXT, " +
                "$TASK_DEADLINE TEXT, " +
                "$TASK_STATUS TEXT NOT NULL, " +
                "FOREIGN KEY ($TASK_CATEGORY) REFERENCES $TABLE_CATEGORIES ($CATEGORY_NAME));"
            )

        val INSERT_TASKS = (
            "INSERT INTO $TABLE_TASKS ($TASK_CATEGORY, $TASK_DESCRIPTION, $TASK_DEADLINE, $TASK_STATUS) VALUES " +
                "('Menage', 'Passer le balais', '11-03-2023 10:00', 'En retard')," +
                "('Courses', 'Acheter du pain', '11-05-2023 10:00', 'A faire')," +
                "('Animaux', 'Prendre RDV Chez le vétérinaire', '11-02-2023 10:00', 'Terminee'),"+
                "('Medical', 'Rappel Vaccin', null, 'A faire');")

        db?.execSQL(CREATE_CATEGORY_TABLE)
        db?.execSQL(INSERT_CATEGORIES)
        db?.execSQL(CREATE_TASK_TABLE)
        db?.execSQL(INSERT_TASKS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    fun addTask(task: TaskModelClass): Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TASK_CATEGORY, task.taskCategory)
        contentValues.put(TASK_DESCRIPTION, task.taskDescription)
        contentValues.put(TASK_DEADLINE, task.taskDeadLine)
        contentValues.put(TASK_STATUS, task.taskStatus)

        val success = db.insert(TABLE_TASKS, null, contentValues)

        db.close()

        return success
    }

    fun selectAllTasks():ArrayList<TaskModelClass>{
        val taskList:ArrayList<TaskModelClass> = ArrayList<TaskModelClass>()
        val selectQuery = "SELECT $TASK_ID, $CATEGORY_ICON_UNICODE, $TASK_DESCRIPTION, $TASK_DEADLINE, $TASK_STATUS " +
                            "FROM $TABLE_TASKS T INNER JOIN $TABLE_CATEGORIES C ON T.$TASK_CATEGORY = C.$CATEGORY_NAME"
        val db = this.readableDatabase
        var cursor: Cursor? = null
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
        var taskStatus: String
        if (cursor.moveToFirst()) {
            do {
                taskId = cursor.getInt(cursor.getColumnIndex(TASK_ID))
                taskCategory = cursor.getInt(cursor.getColumnIndex(CATEGORY_ICON_UNICODE))
                taskDescription = cursor.getString(cursor.getColumnIndex(TASK_DESCRIPTION))
                taskDeadline = cursor.getString(cursor.getColumnIndex(TASK_DEADLINE))
                taskStatus = cursor.getString(cursor.getColumnIndex(TASK_STATUS))
                val task = TaskModelClass(taskId, taskCategory, taskDescription, taskDeadline, taskStatus)
                taskList.add(task)
            } while (cursor.moveToNext())
        }
        return taskList
    }

    fun selectTaskFromId(id: Int): TaskModelClass?{
        val selectQuery = "SELECT $TASK_ID, $CATEGORY_ICON_UNICODE, $TASK_DESCRIPTION, $TASK_DEADLINE, $TASK_STATUS " +
                "FROM $TABLE_TASKS T INNER JOIN $TABLE_CATEGORIES C ON T.$TASK_CATEGORY = C.$CATEGORY_NAME " +
                "WHERE $TASK_ID=$id"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        }
        catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return null
        }
        val taskId: Int
        val taskCategory: Int
        val taskDescription: String
        val taskDeadline: String?
        val taskStatus: String

        cursor.moveToFirst()
        taskId = cursor.getInt(cursor.getColumnIndex(TASK_ID))
        taskCategory = cursor.getInt(cursor.getColumnIndex(CATEGORY_ICON_UNICODE))
        taskDescription = cursor.getString(cursor.getColumnIndex(TASK_DESCRIPTION))
        taskDeadline = cursor.getString(cursor.getColumnIndex(TASK_DEADLINE))
        taskStatus = cursor.getString(cursor.getColumnIndex(TASK_STATUS))
        val task = TaskModelClass(taskId, taskCategory, taskDescription, taskDeadline, taskStatus)

        return task

    }
}