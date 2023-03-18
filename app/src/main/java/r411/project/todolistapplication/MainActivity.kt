package r411.project.todolistapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import r411.project.todolistapplication.adapter.MyGridAdapter
import r411.project.todolistapplication.classes.TaskModelClass
import r411.project.todolistapplication.handler.DatabaseHandler
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    lateinit var mainHandler: Handler

    private val updateTasks = object : Runnable {
        override fun run() {
            //Method calls and updating task status here
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbhandler: DatabaseHandler = DatabaseHandler(this)

        /*dbhandler.addTask(TaskModelClass(1, "Ménage", "Passer l'aspirateur", "11-03-2023 10:00", "En retard"))
        dbhandler.addTask(TaskModelClass(2, "Courses", "Acheter du pain", "15-03-2023 16:29", "A faire"))
        dbhandler.addTask(TaskModelClass(3, "Courses", "Acheter du pain", "14-03-2023 10:00", "Terminee"))
        dbhandler.addTask(TaskModelClass(4, "Courses", "Acheter du pain", null, "A faire"))
*/
        val taskList: ArrayList<TaskModelClass> = dbhandler.selectAllTasks()
        val adapter = MyGridAdapter(this, taskList)
        findViewById<GridView>(R.id.content).adapter = adapter

        mainHandler = Handler(Looper.getMainLooper())
    }

    fun addTask(view: View){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.add_dialog, null)
        dialogBuilder.setView(dialogView)

        val b = dialogBuilder.create()

        dialogView.findViewById<Button>(R.id.button2).setOnClickListener{
            Toast.makeText(applicationContext, "task added", Toast.LENGTH_LONG).show()
            b.dismiss()
        }
        b.show()

    }

    fun viewTask(view: View){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.details_dialog, null)
        dialogBuilder.setView(dialogView)

        val b = dialogBuilder.create()
        b.show()
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTasks)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTasks)
    }
}