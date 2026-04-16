package com.example.spacewaypoint.data

data class TrainingTask(
    val id: Int,
    val title: String,
    val description: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val isBlocked: Boolean = false,
    val waitingTime: Int = 0,
    val complexity: TaskComplexity
)

enum class GameDifficulty{
    EASY,
    NORMAL,
    HARD
}
enum class TaskComplexity {
    LOW, MEDIUM, HIGH, VERYHIGH;
    fun getParams(gameDifficulty: GameDifficulty): TaskParams {
        return when (gameDifficulty) {
            GameDifficulty.EASY -> when (this) {
                LOW -> TaskParams(5, 0, 20)
                MEDIUM -> TaskParams(10, 0, 25)
                HIGH -> TaskParams(15,0, 30)
                VERYHIGH -> TaskParams(25,0, 35)
            }
            GameDifficulty.NORMAL -> when (this) {
                LOW -> TaskParams(5, 5,20)
                MEDIUM -> TaskParams(10, 10,25)
                HIGH -> TaskParams(15, 15,30)
                VERYHIGH -> TaskParams(25, 25,35)
            }
            GameDifficulty.HARD -> when (this) {
                LOW -> TaskParams(3,5, 25)
                MEDIUM -> TaskParams(7,9, 30)
                HIGH -> TaskParams(12,15, 40)
                VERYHIGH -> TaskParams(20,25, 50)
            }
        }
    }
}

data class TaskParams(val valueAdd: Int, val valDiv: Int, val blockedTime: Int)

