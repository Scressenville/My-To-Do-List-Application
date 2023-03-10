package r411.project.todolistapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import r411.project.todolistapplication.R
import r411.project.todolistapplication.classes.TaskModelClass

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

        post_it_banner.setText(String(Character.toChars(0x1f6d2)))
        post_it_content.setText(task!!.taskDescription)
        return listitemView
    }
}