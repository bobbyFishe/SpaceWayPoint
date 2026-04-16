package com.example.spacewaypoint.ui

import com.example.spacewaypoint.data.GameDifficulty
import com.example.spacewaypoint.data.TaskList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class TrainingViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        TaskList.reset()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun `when viewModel initialized then default state is correct`() {
        val viewModel = TrainingViewModel(GameDifficulty.EASY)

        val state = viewModel.uiState.value

        assertEquals(100, state.oxygenPercent)
        assertEquals(0, state.oxygenStock)
        assertEquals(0, state.taskSolved)

        val expectedTaskCount = TaskList.getTasks().size
        assertEquals(expectedTaskCount, viewModel.getSizeTaskList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `check oxygen drain after 3 seconds`() = runTest {
        val viewModel = TrainingViewModel(GameDifficulty.EASY)

        assertEquals(100, viewModel.uiState.value.oxygenPercent)

        testDispatcher.scheduler.advanceTimeBy(1001)
        testDispatcher.scheduler.runCurrent()
        assertEquals(99, viewModel.uiState.value.oxygenPercent)

        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(98, viewModel.uiState.value.oxygenPercent)

        testDispatcher.scheduler.advanceTimeBy(98000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(0, viewModel.uiState.value.oxygenPercent)
        assertEquals(GameStatus.LOST, viewModel.uiState.value.status)

    }

    @Test
    fun `when toggleTask called then expandedTaskId updates correctly`() {
        val viewModel = TrainingViewModel(GameDifficulty.EASY)
        val taskId = 1

        assertEquals(null, viewModel.expandedTaskId.value)

        viewModel.toggleTask(taskId)
        assertEquals(taskId, viewModel.expandedTaskId.value)

        viewModel.toggleTask(taskId)
        assertEquals(null, viewModel.expandedTaskId.value)

        viewModel.toggleTask(taskId)
        viewModel.toggleTask(2)
        assertEquals(2, viewModel.expandedTaskId.value)
    }

    @Test
    fun `task delete`() {
        val viewModel = TrainingViewModel(GameDifficulty.EASY)
        val sizeTasks = viewModel.getSizeTaskList()
        viewModel.delTask(1)
        println(sizeTasks)
        assertEquals("Должно быть меньше на 1", sizeTasks - 1, viewModel.getSizeTaskList())
        val taskExists = viewModel.uiState.value.tasks.any { it.id == 1 }
        assertFalse("Задача с ID 1 должна исчезнуть из списка", taskExists)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `restoreOxygen test` () {
        // увеличение основного у которого меньше 100 при нулевых запасов
        val viewModel = TrainingViewModel(GameDifficulty.EASY)
        assertEquals(100, viewModel.uiState.value.oxygenPercent)
        assertEquals(0, viewModel.uiState.value.oxygenStock)
        testDispatcher.scheduler.advanceTimeBy(10001)
        testDispatcher.scheduler.runCurrent()
        assertEquals(90, viewModel.uiState.value.oxygenPercent)
        viewModel.restoreOxygen()
        assertEquals(90, viewModel.uiState.value.oxygenPercent)

        // увеличение основного у которого 100 при запасе 10
        viewModel.restartGame()
        assertEquals(100, viewModel.uiState.value.oxygenPercent)
        viewModel.handleTaskResult(taskId = 1, taskIsCorrect = true, 0)
        assertEquals(10, viewModel.uiState.value.oxygenStock)
        viewModel.restoreOxygen()
        assertEquals(100, viewModel.uiState.value.oxygenPercent)
        assertEquals(10, viewModel.uiState.value.oxygenStock)

        // увеличение основного у которого 50 при запасе 50
        viewModel.restartGame()
        assertEquals(100, viewModel.uiState.value.oxygenPercent)
        repeat(5) {
            viewModel.handleTaskResult(taskId = it, taskIsCorrect = true, 0)
        }
        assertEquals(50, viewModel.uiState.value.oxygenStock)
        testDispatcher.scheduler.advanceTimeBy(50001)
        testDispatcher.scheduler.runCurrent()
        assertEquals(50, viewModel.uiState.value.oxygenPercent)
        viewModel.restoreOxygen()
        assertEquals(100, viewModel.uiState.value.oxygenPercent)
        assertEquals(0, viewModel.uiState.value.oxygenStock)

        // увеличение основного у которого 50 при запасе 60
        viewModel.restartGame()
        assertEquals(100, viewModel.uiState.value.oxygenPercent)
        repeat(6) {
            viewModel.handleTaskResult(taskId = it, taskIsCorrect = true, 0)
        }
        assertEquals(60, viewModel.uiState.value.oxygenStock)
        testDispatcher.scheduler.advanceTimeBy(50001)
        testDispatcher.scheduler.runCurrent()
        assertEquals(50, viewModel.uiState.value.oxygenPercent)
        viewModel.restoreOxygen()
        assertEquals(100, viewModel.uiState.value.oxygenPercent)
        assertEquals(10, viewModel.uiState.value.oxygenStock)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when answer incorrect then task is blocked and unblocked after time`() = runTest {
        val viewModel = TrainingViewModel(GameDifficulty.EASY)
        val taskId = 1

        viewModel.handleTaskResult(taskId, taskIsCorrect = false, 0)

        val blockedTask = viewModel.uiState.value.tasks.find { it.id == taskId }
        assertEquals(true, blockedTask?.isBlocked)

        val expectedTime = blockedTask?.complexity?.getParams(GameDifficulty.EASY)?.blockedTime ?: 0
        assertEquals(expectedTime, blockedTask?.waitingTime)

        testDispatcher.scheduler.advanceTimeBy(2001)
        testDispatcher.scheduler.runCurrent()

        val tickingTask = viewModel.uiState.value.tasks.find { it.id == taskId }
        assertEquals(expectedTime - 2, tickingTask?.waitingTime)
        assertEquals(true, tickingTask?.isBlocked)

        testDispatcher.scheduler.advanceTimeBy(expectedTime * 1000L) // в миллисекундах
        testDispatcher.scheduler.runCurrent()

        val finalTask = viewModel.uiState.value.tasks.find { it.id == taskId }
        assertEquals(false, finalTask?.isBlocked)
        assertEquals(0, finalTask?.waitingTime)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `game status changes correctly`() = runTest {
        val viewModel = TrainingViewModel(GameDifficulty.EASY)

        testDispatcher.scheduler.advanceTimeBy(100_001)
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, viewModel.uiState.value.oxygenPercent)
        assertEquals(GameStatus.LOST, viewModel.uiState.value.status)

        viewModel.restartGame()
        assertEquals(GameStatus.ACTIVE, viewModel.uiState.value.status)
        assertEquals(100, viewModel.uiState.value.oxygenPercent)

        val totalTasks = viewModel.uiState.value.tasks.size

        repeat(totalTasks) { i ->
            val taskId = viewModel.uiState.value.tasks.first().id
            viewModel.handleTaskResult(taskId, taskIsCorrect = true, 0)
        }

        assertEquals(GameStatus.WON, viewModel.uiState.value.status)
    }



}