package r411.project.todolistapplication.handler

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
                "$CATEGORY_ICON_UNICODE TEXT NOT NULL)"
        )

        val INSERT_CATEGORIES = (
            "INSERT INTO $TABLE_CATEGORIES VALUES" +
                "('Courses', '0x1f6d2')" +
                "('Menage', '0x1f9f9')" +
                "('Travail', '0x270f')" +
                "('Evenement', '0x1f38a')" +
                "('Cuisine', '0x1f370')" +
                "('Medical', '0x1fa7a')" +
                "('Famille', '0x1f468')" +
                "('Animaux', '0x1f407')" +
                "('Autres', '0x1f4d4')"
        )

        val CREATE_TASK_TABLE = (
                "CREATE TABLE $TABLE_TASKS (" +
                    "$TASK_ID INTEGER PRIMARY KEY," +
                    "$TASK_CATEGORY TEXT NOT NULL," +
                    "$TASK_DESCRIPTION TEXT," +
                    "$TASK_DEADLINE TEXT," +
                    "$TASK_STATUS TEXT NOT NULL," +
                    "FOREIGN KEY ($TASK_CATEGORY) REFERENCES $TABLE_CATEGORIES ($CATEGORY_NAME))"
                )
        db?.execSQL(CREATE_CATEGORY_TABLE)
        db?.execSQL(INSERT_CATEGORIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }
}