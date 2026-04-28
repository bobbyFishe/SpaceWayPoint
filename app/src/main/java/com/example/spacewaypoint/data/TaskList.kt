package com.example.spacewaypoint.data

import com.example.spacewaypoint.data.TaskRepository

object TaskList {
    // Теперь это функция, а не переменная.
    // Каждый раз, когда ты её вызываешь, она создает НОВЫЙ список.
    fun getFreshTasks(): List<TrainingTask> {
        return TaskRepository.getQuizTasks()
            .groupBy { it.complexity }
            .flatMap { (_, tasks) ->
                tasks.shuffled().take(10)
            }
            .shuffled()
    }
}
