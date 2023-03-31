package r411.project.todolistapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
            listitemView = LayoutInflater.from(context).inflate(R.layout.post_it, parent, false)
        }

        val task: TaskModelClass? = getItem(position)
        val postItBanner = listitemView!!.findViewById<TextView>(R.id.post_it_shade)
        val postItContent = listitemView.findViewById<TextView>(R.id.post_it_content)

        val postItLayout = listitemView.findViewWithTag<LinearLayout>("postItLayout")
        postItLayout.id = task!!.taskId

        if (task.taskDeadLine != null && task.taskStatus != 1) {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm")
            val minutesDifference = TimeUnit.MINUTES.convert((dateFormat.parse(task.taskDeadLine).time - Date().time), TimeUnit.MILLISECONDS)
            if (minutesDifference < 0) {
                task.taskStatus = -1
                val db = DatabaseHandler(this.context)
                db.changeStatus(task.taskId, -1)
            }
            if (task.taskStatus == -1 && minutesDifference >= 0){
                task.taskStatus = 0
                val db = DatabaseHandler(this.context)
                db.changeStatus(task.taskId, 0)
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

        if (task.taskStatus == 1) {
            postItBanner.setBackgroundResource(R.color.blue_post_it_dark)
            postItContent.setBackgroundResource(R.color.blue_post_it)
        }

        if (task.taskStatus == 0 && task.taskDeadLine == null) {
            postItBanner.setBackgroundResource(R.color.yellow_post_it_dark)
            postItContent.setBackgroundResource(R.color.yellow_post_it)
        }

        postItBanner.text = String(Character.toChars(task.taskCategory))
        postItContent.text = task.taskDescription

        return listitemView
    }
}