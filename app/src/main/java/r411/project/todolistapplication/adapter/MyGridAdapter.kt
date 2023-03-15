package r411.project.todolistapplication.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import r411.project.todolistapplication.R
import r411.project.todolistapplication.classes.TaskModelClass
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MyGridAdapter(context: Context, taskArrayList: ArrayList<TaskModelClass>) :
    ArrayAdapter<TaskModelClass>(context, R.layout.post_it, taskArrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var listitemView = convertView
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(context).inflate(R.layout.post_it, parent, false)
        }

        val task: TaskModelClass? = getItem(position)
        val post_it_banner = listitemView!!.findViewById<TextView>(R.id.post_it_shade)
        val post_it_content = listitemView!!.findViewById<TextView>(R.id.post_it_content)

        if (task!!.taskDeadLine != null) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val minutesDifference = TimeUnit.MINUTES.convert((dateFormat.parse(task.taskDeadLine).time - Date().time), TimeUnit.MILLISECONDS)

            if (minutesDifference < 0) {
                post_it_banner.setBackgroundResource(R.color.red_post_it_dark)
                post_it_content.setBackgroundResource(R.color.red_post_it)
            }

            if (minutesDifference >= 0) {
                post_it_banner.setBackgroundResource(R.color.green_post_it_dark)
                post_it_content.setBackgroundResource(R.color.green_post_it)
            }
        }

        if (task.taskDeadLine == null) {
            post_it_banner.setBackgroundResource(R.color.yellow_post_it_dark)
            post_it_content.setBackgroundResource(R.color.yellow_post_it)
        }

        if (task.taskStatus == "Terminee") {
            post_it_banner.setBackgroundResource(R.color.blue_post_it_dark)
            post_it_content.setBackgroundResource(R.color.blue_post_it)
        }

        post_it_banner.setText(String(Character.toChars(0x1f6d2)))
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        listitemView.id = task.taskId
        post_it_content.setText(task.taskDescription + " " + listitemView.id)
        return listitemView
    }
}