package r411.project.todolistapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import r411.project.todolistapplication.adapter.MyGridAdapter
import r411.project.todolistapplication.classes.TaskModelClass
import r411.project.todolistapplication.handler.DatabaseHandler
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var mainHandler: Handler
    val fullDateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm")
    val dayFormat = SimpleDateFormat("dd MMMM yyyy")
    val timeFormat = SimpleDateFormat("HH:mm")
    var taskStatusFilter: Int? = null

    private val updateTasks = object : Runnable {
        override fun run() {
            //Method calls and updating task status here
            val adapter: MyGridAdapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
            adapter.taskArrayList.sortWith(compareBy{it.taskStatus})
            if (taskStatusFilter != null) {
                adapter.taskArrayList.forEach{ it -> if (it.taskStatus != taskStatusFilter) adapter.taskArrayList.remove(it)}
            }
            adapter.notifyDataSetChanged()

            mainHandler.postDelayed(this, 10000)
        }
    }

    private val notifyLateTasks = object : Runnable {
        override fun run() {
            val dbHandler = DatabaseHandler(applicationContext)
            val taskList = dbHandler.selectAllTasks()
            var lateTasksSize = 0
            taskList.forEach{ task ->
                if (task.taskDeadLine != null && task.taskStatus != 1) {
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm")
                    val minutesDifference = TimeUnit.MINUTES.convert((dateFormat.parse(task.taskDeadLine).time - Date().time), TimeUnit.MILLISECONDS)
                    if (task.taskStatus != -1 && minutesDifference < 0) {
                        task.taskStatus = -1
                        dbHandler.changeStatus(task.taskId, -1)
                        lateTasksSize++
                    }
                }
            }
            if (lateTasksSize != 0) sendNotification(lateTasksSize)

            mainHandler.postDelayed(this, 60000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbhandler = DatabaseHandler(this)
        val taskList: ArrayList<TaskModelClass> = dbhandler.selectAllTasks()
        taskList.sortWith(compareBy { it.taskStatus })
        val adapter = MyGridAdapter(this, taskList)
        val gridView = findViewById<GridView>(R.id.content)
        gridView.adapter = adapter

        val inflater = this.layoutInflater
        val parentLayout = (gridView.parent as LinearLayout)
        val topView: View

        if (taskList.size != 0) {
            topView = inflater.inflate(R.layout.filter_buttons, null)
        }
        else {
            topView = inflater.inflate(R.layout.empty_grid_textview, null)
        }
        parentLayout.addView(topView, 0)

        findViewById<FloatingActionButton>(R.id.add_task_btn).setOnClickListener{ showAddTaskDialog(null, null) }
        mainHandler = Handler(Looper.getMainLooper())

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("NotificationChannelId", "NotificationChannel", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTasks)
        mainHandler.post(notifyLateTasks)
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

        dialogView.findViewById<ImageView>(R.id.detail_close_button).setOnClickListener{ b.dismiss() }

        dialogView.findViewById<Button>(R.id.delete_task).setOnClickListener{ deleteTask(view, b) }

        dialogView.findViewById<Button>(R.id.reprogram_task).setOnClickListener{
            task.taskId = -1
            showAddTaskDialog(null, task)
            b.dismiss()
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

        dialogView.findViewById<FloatingActionButton>(R.id.detail_close_button).setOnClickListener{ b.dismiss() }

        dialogView.findViewById<Button>(R.id.delete_task).setOnClickListener{ deleteTask(view, b) }

        dialogView.findViewById<Button>(R.id.finish_task).setOnClickListener{ finishTask(view, b) }

        dialogView.findViewById<FloatingActionButton>(R.id.detail_modify_button).setOnClickListener{
            showAddTaskDialog(null, task)
            b.dismiss()
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

        if(task != null && task.taskId != -1) {
            dropdown.setSelection(dbhandler.getCategoryIdByEmoji(task.taskCategory)-1)
            dialogView.findViewById<EditText>(R.id.task_description_input).setText(task.taskDescription)
            if (task.taskDeadLine != null) {
                dialogView.findViewById<TextView>(R.id.date_picker_text).text = dayFormat.format(fullDateFormat.parse(task.taskDeadLine).time)
                dialogView.findViewById<TextView>(R.id.time_picker_text).text = timeFormat.format(fullDateFormat.parse(task.taskDeadLine).time)
            }
            btn.text = getString(R.string.btn_modify_task)
            btn.setOnClickListener{
                modifyTask(dialogView, b, task.taskId)
            }
        }
        if (task != null && task.taskId == -1) {
            dropdown.setSelection(dbhandler.getCategoryIdByEmoji(task.taskCategory)-1)
            dialogView.findViewById<EditText>(R.id.task_description_input).setText(task.taskDescription)
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

        dialogBuilder.setPositiveButton(getString(R.string.picker_confirm)) { _, _ ->
            view.findViewById<TextView>(R.id.date_picker_text).text = datePicker.getDate()
        }

        dialogBuilder.setNegativeButton(getString(R.string.cancel_btn)) { _, _ -> /*pass*/ }

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

        dialogBuilder.setPositiveButton(getString(R.string.picker_confirm)) { _, _ ->
            val formattedTime =
                timeFormat.format(timeFormat.parse(timePicker.hour.toString() + ":" + timePicker.minute.toString()))
            view.findViewById<TextView>(R.id.time_picker_text).text = formattedTime
        }

        dialogBuilder.setNegativeButton(getString(R.string.cancel_btn)) {_, _ -> /*pass*/ }

        val b = dialogBuilder.create()

        b.show()
    }

    fun DatePicker.getDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        return dayFormat.format(calendar.time)
    }

    fun modifyTask(view: View, dialog: AlertDialog, taskId: Int) {
        val category = view.findViewById<Spinner>(R.id.dropdown).selectedItemPosition + 1
        val description = view.findViewById<EditText>(R.id.task_description_input).text.toString()
        var deadline: String? = null
        val defaultDay = dayFormat.format(Date())
        val defaultTime = getString(R.string.picker_default_time)

        val datePicked = dialog.findViewById<TextView>(R.id.date_picker_text)
        val timePicked = dialog.findViewById<TextView>(R.id.time_picker_text)

        if (datePicked!!.text != getString(R.string.default_date_value)) {
            deadline = datePicked.text.toString() + " " + defaultTime
        }
        if (timePicked!!.text != getString(R.string.default_time_value)) {
            deadline = defaultDay + " " + timePicked.text.toString().trim()
        }
        if (datePicked.text != getString(R.string.default_date_value) && timePicked.text != getString(R.string.default_time_value)){
            deadline = datePicked.text.toString() + " " + timePicked.text.toString().trim()
        }

        val databaseHandler = DatabaseHandler(this)

        if(description.trim()!=""){
            val status = databaseHandler.modifyTask(taskId, category, description, deadline)
            if(status > -1){
                Toast.makeText(applicationContext,getString(R.string.task_modified_confirmation),Toast.LENGTH_LONG).show()
                val adapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
                val taskIndex = adapter.taskArrayList.indexOfFirst { it.taskId == taskId }
                val modifiedTask = databaseHandler.selectTaskFromId(taskId)
                if (modifiedTask != null) {
                    if (modifiedTask.taskStatus == taskStatusFilter || taskStatusFilter == null) adapter.taskArrayList.set(taskIndex, modifiedTask)
                    else adapter.taskArrayList.removeAt(taskIndex)
                    adapter.notifyDataSetChanged()
                }
                dialog.dismiss()
            }
        }else{
            Toast.makeText(applicationContext,getString(R.string.description_cannot_be_empty),Toast.LENGTH_LONG).show()
        }
    }

    fun insertTask(view: View, dialog: AlertDialog){
        val category = view.findViewById<Spinner>(R.id.dropdown).selectedItemPosition + 1
        val description = view.findViewById<EditText>(R.id.task_description_input).text.toString()
        var deadline: String? = null
        val defaultDay = dayFormat.format(Date())
        val defaultTime = getString(R.string.picker_default_time)

        val datePicked = dialog.findViewById<TextView>(R.id.date_picker_text)
        val timePicked = dialog.findViewById<TextView>(R.id.time_picker_text)

        if (datePicked!!.text != getString(R.string.default_date_value)) {
            deadline = datePicked.text.toString() + " " + defaultTime
        }
        if (timePicked!!.text != getString(R.string.default_time_value)) {
            deadline = defaultDay + " " + timePicked.text.toString().trim()
        }
        if (datePicked.text != getString(R.string.default_date_value) && timePicked.text != getString(R.string.default_time_value)){
            deadline = datePicked.text.toString() + " " + timePicked.text.toString().trim()
        }

        val databaseHandler = DatabaseHandler(this)

        if(description.trim()!=""){
            val status = databaseHandler.addTask(category, description, deadline)
            if(status > -1){
                Toast.makeText(applicationContext,getString(R.string.add_task_confirmation),Toast.LENGTH_LONG).show()
                val taskList: ArrayList<TaskModelClass> = databaseHandler.selectAllTasks()
                val insertedTask = taskList[taskList.size-1]
                val gridView = findViewById<GridView>(R.id.content)
                val adapter = gridView.adapter as MyGridAdapter
                if (adapter.taskArrayList.size == 0 && taskStatusFilter == null) {
                    val inflater = this.layoutInflater
                    val parentLayout = gridView.parent as LinearLayout
                    parentLayout.removeView(findViewById<TextView>(R.id.empty).parent as View)
                    parentLayout.addView(inflater.inflate(R.layout.filter_buttons, null), 0)
                }
                if (taskStatusFilter != null && insertedTask.taskStatus == taskStatusFilter || taskStatusFilter == null) {
                    adapter.taskArrayList.add(insertedTask)
                    adapter.notifyDataSetChanged()
                }
                dialog.dismiss()
            }
        }else{
            Toast.makeText(applicationContext,getString(R.string.description_cannot_be_empty),Toast.LENGTH_LONG).show()
        }
    }

    fun deleteTask(view: View, dialog: AlertDialog){
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.setTitle(getString(R.string.delete_dialog_title))
        dialogBuilder.setMessage(getString(R.string.delete_dialog_message))
        dialogBuilder.setPositiveButton(getString(R.string.confirm_btn)) { _, _ ->
            val deleteId = view.id
            val databaseHandler = DatabaseHandler(this)
            val deletedTask = databaseHandler.selectTaskFromId(deleteId)

            val status = databaseHandler.deleteTask(deleteId)
            if(status > -1){
                Toast.makeText(applicationContext,getString(R.string.task_delete_confirmation),Toast.LENGTH_LONG).show()
                val gridView = findViewById<GridView>(R.id.content)
                val adapter: MyGridAdapter = gridView.adapter as MyGridAdapter
                adapter.taskArrayList.remove(deletedTask)
                if (adapter.taskArrayList.size == 0) {
                    val inflater = this.layoutInflater
                    val parentLayout = gridView.parent as LinearLayout
                    parentLayout.removeView(findViewById<LinearLayout>(R.id.filters).parent as View)
                    parentLayout.addView(inflater.inflate(R.layout.empty_grid_textview, null), 0)
                }
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }
        dialogBuilder.setNegativeButton(getString(R.string.cancel_btn)) { _, _ -> /*pass*/ }

        val b = dialogBuilder.create()
        b.show()
    }

    fun finishTask(view: View, dialog: AlertDialog){
        val databaseHandler = DatabaseHandler(this)
        val updatedTask = databaseHandler.selectTaskFromId(view.id)
        val result = databaseHandler.changeStatus(view.id, 1)
        if (result > -1) {
            Toast.makeText(applicationContext,getString(R.string.task_finished_confirmation),Toast.LENGTH_LONG).show()
            val adapter: MyGridAdapter = findViewById<GridView>(R.id.content).adapter as MyGridAdapter
            adapter.taskArrayList[adapter.taskArrayList.indexOf(updatedTask)].taskStatus = 1
            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }
    }

    fun showAllTasks(view: View) {
        taskStatusFilter = null
        val databaseHandler = DatabaseHandler(this)
        val tasks = databaseHandler.selectAllTasks()
        tasks.sortWith(compareBy { it.taskStatus })
        findViewById<GridView>(R.id.content).adapter = MyGridAdapter(this, tasks)
    }

    fun showFilteredTasks(view: View) {
        val statusFilter = (view.tag as String).toInt()
        taskStatusFilter = statusFilter
        val databaseHandler = DatabaseHandler(this)
        val filteredTasks = databaseHandler.selectTasksWithStatus(statusFilter)
        findViewById<GridView>(R.id.content).adapter = MyGridAdapter(this, filteredTasks)
    }

    private fun sendNotification(lateTasks: Int) {
        val builder = NotificationCompat.Builder(this, "NotificationChannelId")
        builder.setContentTitle("Retard !")
        if (lateTasks == 1) builder.setContentTitle("Vous venez de prendre du retard sur 1 tâche !")
        else builder.setContentTitle("Vous venez de prendre du retard sur $lateTasks tâches !")
        builder.setSmallIcon(R.drawable.icon)
        builder.setAutoCancel(true)

        val managerCompat = NotificationManagerCompat.from(this)
        managerCompat.notify(1, builder.build())
    }
}