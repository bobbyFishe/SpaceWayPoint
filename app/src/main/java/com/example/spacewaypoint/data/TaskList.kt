package com.example.spacewaypoint.data

import com.example.spacewaypoint.data.TaskRepository

object TaskList {
    private val _taskList: MutableList<TrainingTask> = TaskRepository.getQuizTasks()
        .groupBy { it.complexity }
        .flatMap { (_, tasks) ->
            tasks.shuffled().take(10)
        }
        .shuffled()
        .toMutableList()
    private var idCounter = _taskList.size

    fun getTasks(): List<TrainingTask> = _taskList.toList()

    fun reset() {
        _taskList.clear()
        _taskList.addAll(TaskRepository.getQuizTasks().shuffled().toMutableList())
    }
//    fun addTask(title: String,
//                desc: String,
//                complexity: TaskComplexity,
//                options: List<String>,
//                correctAnswerIndex: Int) {
//        _taskList.add(
//            TrainingTask(
//                id = idCounter++,
//                title = title,
//                description = desc,
//                complexity = complexity,
//                options = options,
//                correctAnswerIndex = correctAnswerIndex
//            )
//        )
//    }

    fun delTask(taskId: Int): Boolean {
        return _taskList.removeIf { it.id == taskId }
    }

    fun getSizeTaskList(): Int {
        return _taskList.size
    }

}