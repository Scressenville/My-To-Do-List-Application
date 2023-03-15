package r411.project.todolistapplication.classes

class TaskModelClass(
    var taskId: Int,
    val taskCategory: Int,
    val taskDescription: String,
    val taskDeadLine: String?,
    val taskStatus: String
)