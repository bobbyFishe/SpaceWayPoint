package com.example.spacewaypoint.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spacewaypoint.data.GameDifficulty
import com.example.spacewaypoint.data.TaskList
import com.example.spacewaypoint.data.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrainingViewModel(gameDifficulty: GameDifficulty): ViewModel() {
    private val _uiState = MutableStateFlow(TrainingUiState(
        tasks = TaskList.getTasks(),
        gameDifficulty = gameDifficulty))
    private val _expandedTaskId = MutableStateFlow<Int?>(null)
    val expandedTaskId = _expandedTaskId.asStateFlow()
    val uiState = _uiState.asStateFlow()
    val _completeTaskList = MutableStateFlow<List<Int>>(emptyList())

    val taskCount = _uiState.value.tasks.size
    private var oxygenJob: Job? = null

    init {
        startOxygenDrain()
    }

    fun toggleTask(taskId: Int) {
        _expandedTaskId.update { currentId ->
            if (currentId == taskId) null else taskId
        }
    }

//    fun addTask(
//        title: String,
//        desc: String,
//        options: List<String>,
//        correctAnswerIndex: Int,
//        complexity: TaskComplexity
//    ) {
//        TaskList.addTask(
//            title = title,
//            desc = desc,
//            options = options,
//            correctAnswerIndex = correctAnswerIndex,
//            complexity = complexity,
//        )
//        _uiState.update { currentState ->
//            currentState.copy(
//                tasks = TaskList.getTasks()
//            )
//        }
//    }

    fun delTask(taskId: Int) {
        if (TaskList.delTask(taskId)) {
            _uiState.update { currentState ->
                currentState.copy(
                    tasks = TaskList.getTasks()
                )
            }
        }
    }

    fun getSizeTaskList(): Int {
        return _uiState.value.tasks.size
    }

    fun restoreOxygen() {
        _uiState.update { currentState ->
            if (currentState.oxygenStock <= 0 || currentState.oxygenPercent == START_OXYGEN || currentState.countUpOxygen == 0) {
                return@update currentState
            }
            val needed = 100 - currentState.oxygenPercent
            val amountToTake = minOf(needed, currentState.oxygenStock)
            val newCountUpOxygen = currentState.countUpOxygen - 1
            currentState.copy(
                oxygenPercent = currentState.oxygenPercent + amountToTake,
                oxygenStock = currentState.oxygenStock - amountToTake,
                countUpOxygen = newCountUpOxygen
            )
        }
    }

    fun handleTaskResult(
        taskId: Int,
        taskIsCorrect: Boolean,
        index: Int
    ) {
        val task = _uiState.value.tasks.find { it.id == taskId }
        if (taskIsCorrect) {
            _completeTaskList.update { it + taskId }
            task?.let {
                TaskList.delTask(taskId)
                _uiState.update { currentState ->
                    val newSolved = currentState.taskSolved + 1
                    val newStatus =
                        if (newSolved >= taskCount) GameStatus.WON else currentState.status
                    currentState.copy(
                        oxygenStock = currentState.oxygenStock + task.complexity.getParams(_uiState.value.gameDifficulty).valueAdd,
                        tasks = currentState.tasks.filter { it.id != taskId },
                        taskSolved = newSolved,
                        status = newStatus
                    )
                }
            }
        } else {
            handleIncorrectAnswer(taskId, index)
        }
    }

    fun restartGame() {
        TaskList.reset()
        _uiState.value = TrainingUiState(
            tasks = TaskList.getTasks(),
            oxygenPercent = START_OXYGEN,
            oxygenStock = START_OXYGEN_STOCK,
            taskSolved = 0,
            status = GameStatus.ACTIVE,
            gameDifficulty = _uiState.value.gameDifficulty
        )
        startOxygenDrain()
        _completeTaskList.value = emptyList()
    }

    fun handleIncorrectAnswer(taskId: Int, index: Int) {
        val currentStateValue = _uiState.value

        val currentTask = currentStateValue.tasks.getOrNull(index) ?: return
        val newOxygenStock = (currentStateValue.oxygenStock - currentTask.complexity.getParams(_uiState.value.gameDifficulty).valDiv).coerceAtLeast(0)

        val currentIds = currentStateValue.tasks.map { it.id }.toSet()
        val replacementTask = TaskRepository.getQuizTasks()
            .filter { it.complexity == currentTask.complexity && it.id !in currentIds && it.id !in _completeTaskList.value.toSet() }
            .shuffled()
            .firstOrNull()

        _uiState.update { currentState ->
            val mutableTasks = currentState.tasks.toMutableList()
            val taskToLock = (replacementTask ?: currentTask).copy(
                isBlocked = true,
                waitingTime = currentTask.complexity.getParams(_uiState.value.gameDifficulty).blockedTime
            )
            mutableTasks[index] = taskToLock
            currentState.copy(
                tasks = mutableTasks,
                oxygenStock = newOxygenStock
            )
        }
        val finalId = replacementTask?.id ?: taskId
        startTaskCountdown(finalId)
    }



    private fun startTaskCountdown(taskId: Int) {
        viewModelScope.launch {
            while (true) {
                delay(_uiState.value.oxygenLeakInterval.value)
                var isFinished = false
                _uiState.update { currentState ->
                    val updatedTasks = currentState.tasks.map { task ->
                        if (task.id == taskId && task.isBlocked) {
                            val nextTime = task.waitingTime - 1
                            if (nextTime <= 0) {
                                isFinished = true
                                task.copy(isBlocked = false, waitingTime = 0)
                            } else {
                                task.copy(waitingTime = nextTime)
                            }
                        } else {
                            task
                        }
                    }
                    currentState.copy(tasks = updatedTasks)
                }
                if (isFinished) break
            }
        }
    }

    private fun startOxygenDrain() {
        oxygenJob?.cancel()
        oxygenJob = viewModelScope.launch {
            while (true) {
                delay(_uiState.value.oxygenLeakInterval.value)
                _uiState.update { state ->
                    val newOxygen = if (state.oxygenPercent > 0) state.oxygenPercent - 1 else 0
                    val newStatus = if (newOxygen <= 0) GameStatus.LOST else state.status
                    state.copy(oxygenPercent = newOxygen, status = newStatus)
                }
                if (_uiState.value.status == GameStatus.LOST || _uiState.value.status == GameStatus.WON) break
            }
        }
    }
}

class TrainingViewModelFactory(private val difficulty: GameDifficulty) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainingViewModel(difficulty) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}