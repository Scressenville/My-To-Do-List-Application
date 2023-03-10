package r411.project.todolistapplication

import android.os.Bundle
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import r411.project.todolistapplication.adapter.MyGridAdapter
import r411.project.todolistapplication.classes.TaskModelClass
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val emoji = String(Character.toChars(0x1F918))
        val simpleDateFormat = SimpleDateFormat("dd-mm-yyyy HH:mm")
        val test1: TaskModelClass = TaskModelClass(1, "Ménage", "Passer le balais", simpleDateFormat.format(Date()), "A faire")

        val taskList = ArrayList<TaskModelClass>()
        taskList.add(TaskModelClass(1, "Ménage", "Passer l'aspirateur", null, "A faire"))
        taskList.add(TaskModelClass(2, "Courses", "Acheter du pain", null, "A faire"))
        val adapter = MyGridAdapter(this, taskList)
        findViewById<GridView>(R.id.content).adapter = adapter
    }
}