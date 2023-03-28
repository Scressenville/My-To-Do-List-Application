package r411.project.todolistapplication

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
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
import org.w3c.dom.Text
import r411.project.todolistapplication.adapter.MyGridAdapter
import r411.project.todolistapplication.classes.TaskModelClass
import r411.project.todolistapplication.handler.DatabaseHandler
import java.text.SimpleDateFormat
import java.util.*


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

        val dbhandler = DatabaseHandler(this)
        val taskList: ArrayList<TaskModelClass> = dbhandler.selectAllTasks()
        taskList.sortWith(compareBy { it.taskStatus })
        val adapter = MyGridAdapter(this, taskList)
        findViewById<GridView>(R.id.content).adapter = adapter

        findViewById<FloatingActionButton>(R.id.add_task_btn).setOnClickListener{
            showAddTaskDialog(null, null)
        }
        mainHandler = Handler(Looper.getMainLooper())
        Locale.setDefault(Locale.FRANCE)
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTasks)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTasks)
    }

    fun viewTaskHandler(view: View){
        val dbhandler = DatabaseHandler(this)
        val task = dbhandler.selectTaskFromId(view.id)

        if (task!!.taskStatus!=1) {
            viewUnfinishedTask(task, view)
        }
        else {
            viewFinishedTask(task, view)
        }
    }

    private fun viewFinishedTask(task: TaskModelClass, view: View) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater

        val dialogView = inflater.inflate(R.layout.finished_task_details_dialog, null)
        dialogBuilder.setView(dialogView)

        val b = dialogBuilder.create()

        dialogView.findViewById<TextView>(R.id.task_category).text = String(Character.toChars(task.taskCategory))
        val desc = dialogView.findViewById<TextView>(R.id.details_description)
        desc.movementMethod = ScrollingMovementMethod.getInstance();
        desc.text= task.taskDescription

        dialogView.findViewById<ImageView>(R.id.detail_close_button).setOnClickListener{
            b.dismiss()
        }

        dialogView.findViewById<Button>(R.id.delete_task).setOnClickListener{
            deleteTask(view, b)
        }

        dialogView.findViewById<Button>(R.id.reprogram_task).setOnClickListener{
            Toast.makeText(applicationContext, "TODO", Toast.LENGTH_SHORT).show()
        }

        b.show()
    }

    private fun viewUnfinishedTask(task: TaskModelClass, view: View) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater

        val dialogView = inflater.inflate(R.layout.details_dialog, null)

        val lightElements = listOf(
            dialogView.findViewById<LinearLayout>(R.id.details_content),
            dialogView.findViewById<FloatingActionButton>(R.id.detail_modify_button),
            dialogView.findViewById<FloatingActionButton>(R.id.detail_close_button)
        )

        val darkElements = listOf(
            dialogView.findViewById<LinearLayout>(R.id.details_banner),
            dialogView.findViewById<Button>(R.id.finish_task),
            dialogView.findViewById<Button>(R.id.delete_task)
        )

        val shadeColor = (view.findViewById<TextView>(R.id.post_it_shade).background as ColorDrawable).color
        val lightColor = (view.findViewById<TextView>(R.id.post_it_content).background as ColorDrawable).color

        darkElements.forEach{element -> element.backgroundTintList = ColorStateList.valueOf(shadeColor)}
        lightElements.forEach{element -> element.backgroundTintList = ColorStateList.valueOf(lightColor)}

        val taskDeadline = dialogView.findViewById<TextView>(R.id.details_deadline)
        if (task.taskDeadLine != null) taskDeadline.text=task.taskDeadLine
        dialogBuilder.setView(dialogView)

        val b = dialogBuilder.create()

        dialogView.findViewById<TextView>(R.id.task_category).text = String(Character.toChars(task.taskCategory))
        val desc = dialogView.findViewById<TextView>(R.id.details_description)
        desc.movementMethod = ScrollingMovementMethod.getInstance();
        desc.text= task.taskDescription

        dialogView.findViewById<FloatingActionButton>(R.id.detail_close_button).setOnClickListener{
            b.dismiss()
        }

        dialogView.findViewById<FloatingActionButton>(R.id.detail_modify_button).setOnClickListener{
            showAddTaskDialog(null, task)
            b.dismiss()
        }

        dialogView.findViewById<Button>(R.id.delete_task).setOnClickListener{
            deleteTask(view, b)
        }

        dialogView.findViewById<Button>(R.id.finish_task).setOnClickListener{
            finishTask(view, b)
        }

        b.show()
    }

    fun showAddTaskDialog(view: View?, task: TaskModelClass?){
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

        if(task!=null) {
            dropdown.setSelection(dbhandler.getCategoryIdByEmoji(task.taskCategory)-1)
            dialogView.findViewById<EditText>(R.id.task_description_input).setText(task.taskDescription)
            dialogView.findViewById<TextView>(R.id.date_picker_text).text = SimpleDateFormat("dd MMMM yyyy").format(SimpleDateFormat("dd MMMM yyyy HH:mm").parse(task.taskDeadLine).time)
            dialogView.findViewById<TextView>(R.id.time_picker_text).text = SimpleDateFormat("HH:mm").format(SimpleDateFormat("dd MMMM yyyy HH:mm").parse(task.taskDeadLine).time)
            btn.setText("Modifier")
            btn.setOnClickListener{
                modifyTask(dialogView, b, task.taskId)
            }
        }

        b.show()
    }

    fun openDatePicker(view: View){
        Locale.setDefault(Locale.FRANCE)
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.date_picker, null)
        dialogBuilder.setView(dialogView)

        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker1)
        val today = Calendar.getInstance()
        datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)

        ) { view, year, month, day ->
            null
        }

        dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
            view.findViewById<TextView>(R.id.date_picker_text).text = datePicker.getDate()
        })

        dialogBuilder.setNegativeButton("Annuler", DialogInterface.OnClickListener { _, _ ->
            //pass
        })

        val b = dialogBuilder.create()

        b.show()
    }

    fun openTimePicker(view: View){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.time_picker, null)
        dialogBuilder.setView(dialogView)

        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker1)
        timePicker.setIs24HourView(true)

        dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
            val format = SimpleDateFormat("HH:mm")
            val formattedTime = format.format(format.parse(timePicker.hour.toString()+":"+timePicker.minute.toString()))
            view.findViewById<TextView>(R.id.time_picker_text).text = formattedTime
        })

        dialogBuilder.setNegativeButton("Annuler", DialogInterface.OnClickListener { _, _ ->
            //pass
        })

        val b = dialogBuilder.create()

        b.show()
    }

    fun DatePicker.getDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val format = SimpleDateFormat("dd MMMM yyyy")
        return format.format(calendar.time)
    }

    fun modifyTask(view: View, dialog: AlertDialog, taskId: Int) {
        val category = view.findViewById<Spinner>(R.id.dropdown).selectedItemPosition + 1
        //val description = view.findViewById<EditText>(R.id.task_description_input).text.toString().replace("\n", " ")
        val description = view.findViewById<EditText>(R.id.task_description_input).text.toString()
        var deadline: String? = null
        val defaultDay = SimpleDateFormat("dd MMMM yyyy").format(Date())
        val defaultTime = "23:59"

        val datePicked = dialog.findViewById<TextView>(R.id.date_picker_text)
        val timePicked = dialog.findViewById<TextView>(R.id.time_picker_text)

        if (datePicked!!.text != resources.getString(R.string.default_date_value)) {
            deadline = datePicked.text.toString() + " " + defaultTime
        }
        if (timePicked!!.text != resources.getString(R.string.default_time_value)) {
            deadline = defaultDay + " " + timePicked.text.toString().trim()
        }
        if (datePicked.text != resources.getString(R.string.default_date_value) && timePicked.text != resources.getString(R.string.default_time_value)){
            deadline = datePicked.text.toString() + " " + timePicked.text.toString().trim()
        }

        //println(description.lines().take(6).joinToString(separator = "\n"))
        val databaseHandler = DatabaseHandler(this)

        if(description.trim()!=""){
            val status = databaseHandler.modifyTask(taskId, category, description, deadline)
            if(status > -1){
                Toast.makeText(applicationContext,"Tâche modifiée !",Toast.LENGTH_LONG).show()
                val adapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
                val taskIndex = adapter.taskArrayList.indexOfFirst { it.taskId == taskId }
                val modifiedTask = databaseHandler.selectTaskFromId(taskId)
                if (modifiedTask != null) {
                    adapter.taskArrayList.set(taskIndex, modifiedTask)
                }
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }else{
            Toast.makeText(applicationContext,"La description de la tâche ne peut pas être vide.",Toast.LENGTH_LONG).show()
        }
    }

    fun insertTask(view: View, dialog: AlertDialog){
        val category = view.findViewById<Spinner>(R.id.dropdown).selectedItemPosition + 1
        //val description = view.findViewById<EditText>(R.id.task_description_input).text.toString().replace("\n", " ")
        val description = view.findViewById<EditText>(R.id.task_description_input).text.toString()
        var deadline: String? = null
        val defaultDay = SimpleDateFormat("dd MMMM yyyy").format(Date())
        val defaultTime = "23:59"

        val datePicked = dialog.findViewById<TextView>(R.id.date_picker_text)
        val timePicked = dialog.findViewById<TextView>(R.id.time_picker_text)

        if (datePicked!!.text != resources.getString(R.string.default_date_value)) {
            deadline = datePicked.text.toString() + " " + defaultTime
        }
        if (timePicked!!.text != resources.getString(R.string.default_time_value)) {
            deadline = defaultDay + " " + timePicked.text.toString().trim()
        }
        if (datePicked.text != resources.getString(R.string.default_date_value) && timePicked.text != resources.getString(R.string.default_time_value)){
            deadline = datePicked.text.toString() + " " + timePicked.text.toString().trim()
        }

        //println(description.lines().take(6).joinToString(separator = "\n"))
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
                Toast.makeText(applicationContext,"Tâche supprimée",Toast.LENGTH_LONG).show()
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

    fun finishTask(view: View, dialog: AlertDialog){
        val databaseHandler = DatabaseHandler(this)
        val updatedTask = databaseHandler.selectTaskFromId(view.id)
        val result = databaseHandler.changeStatusFinished(view.id)
        if (result > -1) {
            Toast.makeText(applicationContext,"Tâche réalisée !",Toast.LENGTH_LONG).show()
            val adapter: MyGridAdapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
            adapter.taskArrayList[adapter.taskArrayList.indexOf(updatedTask)].taskStatus = 1
            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }
    }
}