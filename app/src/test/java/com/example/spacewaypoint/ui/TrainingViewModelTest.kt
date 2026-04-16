package com.example.spacewaypoint.ui

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
        // Подменяем Main диспетчер на тестовый
        Dispatchers.setMain(testDispatcher)
        TaskList.reset()
    }

    @After
    fun tearDown() {
        // Сбрасываем после теста
        Dispatchers.resetMain()
    }
    @Test
    fun `when viewModel initialized then default state is correct`() {
        // Создаем ViewModel
        val viewModel = TrainingViewModel()

        // Получаем текущее состояние
        val state = viewModel.uiState.value

        // Проверяем начальные значения
        assertEquals(100, state.oxygenPercent)
        assertEquals(0, state.oxygenStock)
        assertEquals(0, state.taskSolved)

        // Проверяем, что список задач загрузился (размер совпадает с TaskList)
        val expectedTaskCount = TaskList.getTasks().size
        assertEquals(expectedTaskCount, viewModel.getSizeTaskList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `check oxygen drain after 3 seconds`() = runTest { // Специальный блок для тестов корутин
        val viewModel = TrainingViewModel()

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
        val viewModel = TrainingViewModel()
        val taskId = 1

        // 1. Изначально ни одна задача не должна быть открыта
        assertEquals(null, viewModel.expandedTaskId.value)

        // 2. Нажимаем на задачу №1 -> она должна открыться
        viewModel.toggleTask(taskId)
        assertEquals(taskId, viewModel.expandedTaskId.value)

        // 3. Нажимаем на ту же задачу №1 еще раз -> она должна закрыться (стать null)
        viewModel.toggleTask(taskId)
        assertEquals(null, viewModel.expandedTaskId.value)

        // 4. Нажимаем на задачу №1, а потом на задачу №2
        viewModel.toggleTask(taskId) // открыли 1
        viewModel.toggleTask(2)      // открыли 2
        assertEquals(2, viewModel.expandedTaskId.value) // первая должна закрыться, вторая открыться
    }

    @Test
    fun `task delete`() {
        val viewModel = TrainingViewModel()
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
        val viewModel = TrainingViewModel()
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
        val viewModel = TrainingViewModel()
        val taskId = 1

        viewModel.handleTaskResult(taskId, taskIsCorrect = false, 0)

        val blockedTask = viewModel.uiState.value.tasks.find { it.id == taskId }
        assertEquals(true, blockedTask?.isBlocked)

        val expectedTime = blockedTask?.complexity?.blockedTime ?: 0
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
        val viewModel = TrainingViewModel()

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