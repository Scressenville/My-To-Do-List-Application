package r411.project.todolistapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import r411.project.todolistapplication.MainActivity
import r411.project.todolistapplication.R
import r411.project.todolistapplication.classes.TaskModelClass
import r411.project.todolistapplication.handler.DatabaseHandler
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MyGridAdapter(context: Context, var taskArrayList: ArrayList<TaskModelClass>) :
    ArrayAdapter<TaskModelClass>(context, R.layout.post_it, taskArrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var listitemView = convertView
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(context).inflate(R.layout.post_it, parent, false)
        }

        val task: TaskModelClass? = getItem(position)
        val postItBanner = listitemView!!.findViewById<TextView>(R.id.post_it_shade)
        val postItContent = listitemView.findViewById<TextView>(R.id.post_it_content)

        val postItLayout = listitemView.findViewWithTag<LinearLayout>("postItLayout")
        postItLayout.id = task!!.taskId

        if (task.taskDeadLine != null && task.taskStatus != 1) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val minutesDifference = TimeUnit.MINUTES.convert((dateFormat.parse(task.taskDeadLine).time - Date().time), TimeUnit.MILLISECONDS)
            if (minutesDifference < 0) {
                task.taskStatus = -1
                val test = DatabaseHandler(this.context)
                test.changeStatusLate(task.taskId)
            }

            if (task.taskStatus == -1) {
                postItBanner.setBackgroundResource(R.color.red_post_it_dark)
                postItContent.setBackgroundResource(R.color.red_post_it)
            }

            if (task.taskStatus == 0) {
                postItBanner.setBackgroundResource(R.color.green_post_it_dark)
                postItContent.setBackgroundResource(R.color.green_post_it)
            }
        }

        if (task.taskDeadLine == null && task.taskStatus == 0) {
            postItBanner.setBackgroundResource(R.color.yellow_post_it_dark)
            postItContent.setBackgroundResource(R.color.yellow_post_it)
        }

        if (task.taskStatus == 1) {
            postItBanner.setBackgroundResource(R.color.blue_post_it_dark)
            postItContent.setBackgroundResource(R.color.blue_post_it)
            listitemView.findViewById<LinearLayout>(task.taskId).setOnClickListener{
                Toast.makeText(this.context, "test", Toast.LENGTH_SHORT).show()
            }
        }

        postItBanner.text = String(Character.toChars(task.taskCategory))
        postItContent.text = task.taskDescription

        return listitemView
    }
}