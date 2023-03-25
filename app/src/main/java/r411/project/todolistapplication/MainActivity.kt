package r411.project.todolistapplication

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import r411.project.todolistapplication.adapter.MyGridAdapter
import r411.project.todolistapplication.classes.TaskModelClass
import r411.project.todolistapplication.handler.DatabaseHandler
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    lateinit var mainHandler: Handler

    private val updateTasks = object : Runnable {
        override fun run() {
            //Method calls and updating task status here
            val adapter: MyGridAdapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
            adapter.notifyDataSetChanged()

            mainHandler.postDelayed(this, 10000)
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

    fun showAddTaskDialog(view: View){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.add_dialog, null)
        dialogBuilder.setView(dialogView)

        val b = dialogBuilder.create()

        val dbhandler = DatabaseHandler(this)
        val categories = dbhandler.selectAllCategories()
        val dropdown = dialogView.findViewById<Spinner>(R.id.dropdown)

        val ad = ArrayAdapter<Any?>(
            this, android.R.layout.simple_spinner_item, categories
        )

        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        dropdown.adapter = ad

        val btn = dialogView.findViewById<Button>(R.id.create_task_btn)
        btn.setOnClickListener{
            insertTask(dialogView, b)
        }

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

    fun viewTask(view: View){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.details_dialog, null)
        dialogBuilder.setView(dialogView)

        val b = dialogBuilder.create()

        val dbhandler = DatabaseHandler(this)
        val task = dbhandler.selectTaskFromId(view.id)

        dialogView.findViewById<TextView>(R.id.task_category).text = String(Character.toChars(task!!.taskCategory))
        dialogView.findViewById<TextView>(R.id.details_description).setMovementMethod(ScrollingMovementMethod.getInstance());
        dialogView.findViewById<TextView>(R.id.details_description).text= task.taskDescription

        val lightElements = listOf(
            dialogView.findViewById<FloatingActionButton>(R.id.detail_close_button),
            dialogView.findViewById<FloatingActionButton>(R.id.detail_modify_button),
            dialogView.findViewById<Button>(R.id.details_content)
        )

        val test = dialogView.findViewById<FloatingActionButton>(R.id.detail_close_button)
        println(test.backgroundTintList)

        val darkElements = listOf(
            dialogView.findViewById<LinearLayout>(R.id.details_banner),
            dialogView.findViewById<Button>(R.id.task_complete),
            dialogView.findViewById<Button>(R.id.delete_task)
        )

        if (task.taskDeadLine == null && task.taskStatus == 0) {
            for (element in lightElements) element.setBackgroundResource(R.color.yellow_post_it)
            for (element in darkElements) element.setBackgroundResource(R.color.yellow_post_it_dark)
        }

        if (task.taskStatus == -1) {
            for (element in lightElements) element.setBackgroundResource(R.color.red_post_it)
            for (element in darkElements) element.setBackgroundResource(R.color.red_post_it_dark)
        }

        if (task.taskStatus == 1) {
            for (element in lightElements) element.setBackgroundResource(R.color.blue_post_it)
            for (element in darkElements) element.setBackgroundResource(R.color.blue_post_it_dark)
        }

        val taskDeadline = dialogView.findViewById<TextView>(R.id.details_deadline)
        if (task.taskDeadLine != null) taskDeadline.text=task.taskDeadLine

        dialogView.findViewById<ImageView>(R.id.detail_close_button).setOnClickListener{
            b.dismiss()
        }

        dialogView.findViewById<Button>(R.id.delete_task).setOnClickListener{
            deleteTask(view, b)
        }

        b.show()
    }

    fun insertTask(view: View, dialog: AlertDialog){
        val category = view.findViewById<Spinner>(R.id.dropdown).selectedItemPosition + 1
        //val description = view.findViewById<EditText>(R.id.task_description_input).text.toString().replace("\n", " ")
        val description = view.findViewById<EditText>(R.id.task_description_input).text.toString()
        val deadline = null

        println(description.lines().take(6).joinToString(separator = "\n"))
        val databaseHandler = DatabaseHandler(this)

        if(description.trim()!=""){
            val status = databaseHandler.addTask(category, description, deadline)
            if(status > -1){
                Toast.makeText(applicationContext,"Tâche ajoutée !",Toast.LENGTH_LONG).show()
                val taskList: ArrayList<TaskModelClass> = databaseHandler.selectAllTasks()
                val insertedTask = taskList[taskList.size-1]
                val adapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
                adapter.taskArrayList.add(insertedTask)
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }else{
            Toast.makeText(applicationContext,"La description de la tâche ne peut pas être vide.",Toast.LENGTH_LONG).show()
        }
    }

    fun deleteTask(view: View, dialog: AlertDialog){
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.setTitle("Suppression de tâche")
        dialogBuilder.setMessage("Êtes vous sûr(e) de vouloir supprimer cette tâche ?")
        dialogBuilder.setPositiveButton("Confirmer", DialogInterface.OnClickListener { _, _ ->

            val deleteId = view.id
            val databaseHandler = DatabaseHandler(this)
            val deletedTask = databaseHandler.selectTaskFromId(deleteId)

            val status = databaseHandler.deleteTask(deleteId)
            if(status > -1){
                Toast.makeText(applicationContext,"Task deleted",Toast.LENGTH_LONG).show()
                val adapter: MyGridAdapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
                adapter.taskArrayList.remove(deletedTask)
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }

        })
        dialogBuilder.setNegativeButton("Annuler", DialogInterface.OnClickListener { _, _ ->
            //pass
        })
        val b = dialogBuilder.create()
        b.show()
    }
}
