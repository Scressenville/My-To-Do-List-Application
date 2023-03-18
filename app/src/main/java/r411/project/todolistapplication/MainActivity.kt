package r411.project.todolistapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
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
            var adapter: MyGridAdapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
            adapter.notifyDataSetChanged()
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbhandler: DatabaseHandler = DatabaseHandler(this)
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

        val categories = arrayOf<String>("Ménage", "Courses", "Animaux")
        val dropdown = dialogView.findViewById<Spinner>(R.id.dropdown)

        val ad = ArrayAdapter<Any?>(
            this, android.R.layout.simple_spinner_item, categories
        )

        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        dropdown.adapter = ad

        /*dialogView.findViewById<Button>(R.id.add_task_btn).setOnClickListener{
            Toast.makeText(applicationContext, "task added", Toast.LENGTH_LONG).show()
            b.dismiss()
        }*/
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

    fun openDatePicker(view: View) {
        println("coucou")
    }
}
