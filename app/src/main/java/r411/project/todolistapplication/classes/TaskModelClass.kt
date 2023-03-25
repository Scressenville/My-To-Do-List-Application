package r411.project.todolistapplication.classes

data class TaskModelClass(
    var taskId: Int,
    val taskCategory: Int,
    val taskDescription: String,
    val taskDeadLine: String?,
    var taskStatus: Int
)