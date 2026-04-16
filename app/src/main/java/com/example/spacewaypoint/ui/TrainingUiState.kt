package com.example.spacewaypoint.ui

import com.example.spacewaypoint.data.GameDifficulty
import com.example.spacewaypoint.data.TrainingTask

const val START_OXYGEN = 100
const val START_OXYGEN_STOCK = 0
data class TrainingUiState(
    val tasks: List<TrainingTask> = emptyList(),
    val oxygenPercent: Int = START_OXYGEN,
    val oxygenStock: Int = START_OXYGEN_STOCK,
    val taskSolved: Int = 0,
    val status: GameStatus = GameStatus.ACTIVE,
    val gameDifficulty: GameDifficulty,
    val countUpOxygen: Int = when(gameDifficulty) {
        GameDifficulty.EASY -> UpOxygen.EASY.value
        GameDifficulty.NORMAL -> UpOxygen.NORMAl.value
        GameDifficulty.HARD -> UpOxygen.HARD.value
}
) {
    val oxygenLeakInterval: OxygenLeakInterval
        get() = when (gameDifficulty) {
            GameDifficulty.EASY -> OxygenLeakInterval.EASY
            GameDifficulty.NORMAL -> OxygenLeakInterval.NORMAl
            GameDifficulty.HARD -> OxygenLeakInterval.HARD
        }
}

enum class GameStatus {
    ACTIVE, WON, LOST
}

enum class UpOxygen(
    val value: Int
) {
    EASY(10), NORMAl(8), HARD(6)
}

enum class OxygenLeakInterval(
    val value: Long
){
    EASY(2000L), NORMAl(1000L), HARD(800L)
}
