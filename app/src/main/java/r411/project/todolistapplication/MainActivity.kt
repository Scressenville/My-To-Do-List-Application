package r411.project.todolistapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    }
}