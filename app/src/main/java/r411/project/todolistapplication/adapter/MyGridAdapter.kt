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
            val date1 = dateFormat.parse(task.taskDeadLine)
            val date2 = Date()
            val diff = date1.time - date2.time
            val diff2 = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)

            if (diff2 <= 1 && diff2 >= 0) {
                post_it_banner.setBackgroundResource(R.color.red_post_it_dark)
                post_it_content.setBackgroundResource(R.color.red_post_it)
            }

            if (diff2>1) {
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
        post_it_content.setText(task.taskDescription)
        return listitemView
    }
}