package com.example.spacewaypoint.ui

import android.media.MediaPlayer
import androidx.annotation.StringRes
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.spacewaypoint.R
import com.example.spacewaypoint.data.TaskComplexity
import com.example.spacewaypoint.data.TaskList
import com.example.spacewaypoint.data.TrainingTask
import com.example.spacewaypoint.ui.theme.SpaceWaypointTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun AppScreen(
    viewModel: TrainingViewModel,
    onExit: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val appUiState by viewModel.uiState.collectAsState()
    val expandedId by viewModel.expandedTaskId.collectAsState()
    val context = LocalContext.current
    val playSound = { resId: Int ->
        MediaPlayer.create(context, resId).apply {
            setVolume(0.1f, 0.1f)
            setOnCompletionListener { release() }
            start()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        GameAudioManager(
            oxygenPercent = appUiState.oxygenPercent,
            gameStatus = appUiState.status
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            OxygenBlog(
                oxygen = appUiState.oxygenPercent,
                oxygenStock = appUiState.oxygenStock,
                countOxygenUp = appUiState.countUpOxygen,
                oxygenText = getOxygenTextRes(appUiState.oxygenPercent),
                oxygenStockText = R.string.oxygen_stock,
                onClick = {
                    viewModel.restoreOxygen()
                    playSound(if (appUiState.oxygenStock > 0 && appUiState.countUpOxygen > 0) R.raw.air else R.raw.error)
                })
            TaskStatsBlock(
                totalTasks = viewModel.taskCount, solvedTasks = appUiState.taskSolved
            )
            TasksBlogList(
                taskList = appUiState.tasks,
                expandedTaskId = expandedId,
                onTaskClick = { id -> viewModel.toggleTask(id) },
                onTaskResult = { id, isCorrect, index ->
                    viewModel.handleTaskResult(id, isCorrect, index)
                })
        }
        GameEndBanner(
            status = appUiState.status,
            onRestart = {
                navController.popBackStack(TrainingAppScreen.Complexity.name, inclusive = false) },
            onExit = onExit
        )
    }
}

@Composable
fun TaskStatsBlock(
    totalTasks: Int, solvedTasks: Int, modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.padding_medium),
            )
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = dimensionResource(R.dimen.padding_medium),
                    end = dimensionResource(R.dimen.padding_medium),
                    bottom = dimensionResource(R.dimen.padding_medium),
                    top = dimensionResource(R.dimen.padding_medium)
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.mission_progress),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.isCompletedTask, solvedTasks, totalTasks),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            val progress = if (totalTasks > 0) solvedTasks.toFloat() / totalTasks else 0f
            CircularProgressIndicator(
                progress = { progress },
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}


@Composable
fun OxygenBlog(
    oxygen: Int,
    oxygenStock: Int,
    countOxygenUp: Int,
    @StringRes oxygenText: Int,
    @StringRes oxygenStockText: Int,
    onClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val stockHighlightColor = remember { Animatable(Color.Transparent) }
    val limitHighlightColor = remember { Animatable(Color.Transparent) }
    var clickTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(oxygenStock) {
        if (oxygenStock > 0) {
            stockHighlightColor.animateTo(
                targetValue = Color.Red.copy(alpha = 0.5f), animationSpec = tween(1000)
            )
            stockHighlightColor.animateTo(
                targetValue = Color.Transparent, animationSpec = tween(500)
            )
        }
    }

    LaunchedEffect(clickTrigger) {
        if (clickTrigger > 0 && countOxygenUp == 0) {
            limitHighlightColor.animateTo(Color.Red.copy(alpha = 0.5f), tween(1000))
            limitHighlightColor.animateTo(Color.Transparent, tween(500))
        }
    }

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.4f, animationSpec = infiniteRepeatable(
            animation = tween(500), // Длительность одного "мига" (0.5 сек)
            repeatMode = RepeatMode.Reverse // Чтобы плавно возвращалось назад
        ), label = "alphaAnimation"
    )

    val baseColor = getOxygenColor(oxygen)

    val finalBackgroundColor = if (oxygen <= 20) {
        baseColor.copy(alpha = alpha)
    } else {
        baseColor.copy(alpha = 0.2f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            //.background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),

        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(oxygenText, oxygen),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .background(
                    color = finalBackgroundColor, shape = MaterialTheme.shapes.medium
                )
                .padding(dimensionResource(R.dimen.padding_small))
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.padding_medium))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    onClick()
                    if (countOxygenUp == 0) clickTrigger++
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.button_oxygen_up),
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)))
                    Text(
                        text = stringResource(
                            R.string.limit_count_up_oxygen,
                            countOxygenUp
                        ) + if (countOxygenUp in 2..4) " раза" else " раз",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .background(limitHighlightColor.value, MaterialTheme.shapes.small)
                            .padding(4.dp)
                    )
                }
            }
//            Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.padding_high)))
            Text(
                text = stringResource(oxygenStockText, oxygenStock),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .background(
                        color = stockHighlightColor.value, shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun TasksBlogList(
    taskList: List<TrainingTask>,
    expandedTaskId: Int?,
    onTaskClick: (Int) -> Unit,
    onTaskResult: (Int, Boolean, Int) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        itemsIndexed(
            items = taskList, key = { index, task -> task.id }) { index, taskItem ->
            TaskItem(
                task = taskItem,
                taskNumber = index + 1,
                expanded = taskItem.id == expandedTaskId,
                onToggle = { onTaskClick(taskItem.id) },
                onAnswerChecked = { result -> onTaskResult(taskItem.id, result, index) },
                listState = listState,
                coroutineScope = coroutineScope,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = tween(durationMillis = 1500),
                    placementSpec = spring(stiffness = Spring.StiffnessVeryLow)
                ),
            )
        }
    }
}

@Composable
fun TaskItem(
    task: TrainingTask,
    taskNumber: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAnswerChecked: (Boolean) -> Unit,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f, label = "Rotation"
    )
    val background = when {
        task.isBlocked -> MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)
        else -> when (task.complexity) {
            TaskComplexity.LOW -> MaterialTheme.colorScheme.tertiaryContainer
            TaskComplexity.MEDIUM -> MaterialTheme.colorScheme.surfaceContainer
            TaskComplexity.HIGH -> MaterialTheme.colorScheme.inversePrimary
            TaskComplexity.VERYHIGH -> MaterialTheme.colorScheme.error
        }
    }
    Card(
        modifier = modifier
            .padding(horizontal = dimensionResource(R.dimen.padding_medium))
            .clickable {
                onToggle()
                coroutineScope.launch {
                    delay(650)
                    listState.animateScrollToItem(taskNumber - 1, scrollOffset = -100)
                }
            },
        shape = CutCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = background, contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_small)),
        ) {
            CardThemeBlog(
                taskTitle = if (!task.isBlocked) task.title else stringResource(
                    R.string.blocked_on, task.waitingTime
                ),
                taskNumber = taskNumber,
                taskComplexity = task.complexity,
                taskIsBlocked = task.isBlocked
            )
            if (!task.isBlocked) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(rotation)
                        .padding(start = 4.dp)
                )
                CardDescBlog(
                    expanded = expanded,
                    taskDesc = task.description,
                    taskOption = task.options,
                    taskCorrectAnswerIndex = task.correctAnswerIndex,
                    onResult = { isCorrect -> onAnswerChecked(isCorrect) })
            }
        }

    }
}

@Composable
fun CardDescBlog(
    expanded: Boolean,
    taskDesc: String,
    taskOption: List<String>,
    taskCorrectAnswerIndex: Int,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val playSound = { resId: Int ->
        MediaPlayer.create(context, resId).apply {
            setOnCompletionListener { release() }
            start()
        }
    }
    AnimatedVisibility(
        visible = expanded, enter = expandVertically(
            expandFrom = Alignment.Top, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(500)),

        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow
            )
        ) + fadeOut()
    )
    {
        Column(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_medium))) {
            Text(
                text = taskDesc,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
            )
            Spacer(modifier = Modifier.padding(4.dp))
            val shuffledOptions = remember(taskOption) {
                taskOption.mapIndexed { index, text -> index to text }.shuffled()
            }
            var selectedOriginalIndex by rememberSaveable(taskOption) { mutableStateOf<Int?>(null) }

            shuffledOptions.forEach { (originalIndex, text) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = dimensionResource(R.dimen.padding_medium))
                        .background(
                            color = if (originalIndex == selectedOriginalIndex)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .selectable(
                            selected = (originalIndex == selectedOriginalIndex),
                            onClick = { selectedOriginalIndex = originalIndex }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (originalIndex == selectedOriginalIndex),
                        onClick = null,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Button(
                onClick = {
                    val isCorrect = (selectedOriginalIndex == taskCorrectAnswerIndex)
                    playSound(if (isCorrect) R.raw.success else R.raw.error)
                    onResult(isCorrect)
                }, enabled = selectedOriginalIndex != null, modifier = Modifier.align(Alignment.End)
            ) {
                Text("Отправить")
            }
        }
    }
}

@Composable
fun CardThemeBlog(
    taskTitle: String,
    taskNumber: Int,
    taskComplexity: TaskComplexity,
    taskIsBlocked: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = taskTitle,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
            color = if (taskComplexity == TaskComplexity.VERYHIGH) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.inverseSurface
        )
        if (!taskIsBlocked) {
            Text(
                text = stringResource(
                    R.string.task_id, taskNumber.toString().padStart(2, '0')
                ),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_medium))
            )
        }
    }
}

@StringRes
fun getOxygenTextRes(percent: Int): Int {
    return when (percent) {
        in 0..20 -> R.string.oxygen_blog_more0
        in 20..50 -> R.string.oxygen_blog_more20
        else -> R.string.oxygen_blog_more50
    }
}

@Composable
fun getOxygenColor(percent: Int): Color {
    return when (percent) {
        in 0..20 -> Color(0xFFFF0019)   // Светло-красный (критический)
        in 21..50 -> Color(0xFFFFE700)  // Светло-желтый (низкий)
        else -> Color(0xFF23AF27)       // Светло-зеленый (норма)
    }
}


@Composable
fun GameEndBanner(
    status: GameStatus,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    if (status != GameStatus.ACTIVE) {
        var isButtonsVisible by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = if (status == GameStatus.WON) "МИССИЯ ВЫПОЛНЕНА" else "СИСТЕМЫ ОТКАЗАЛИ",
                    style = MaterialTheme.typography.headlineMedium
                )
            }, text = {
                TypewriterText(
                    fullText = if (status == GameStatus.WON) stringResource(R.string.won_message) else stringResource(
                        R.string.lose_message
                    ),
                    onFinished = { isButtonsVisible = true }
                )
            }, confirmButton = {
                AnimatedVisibility(
                    visible = isButtonsVisible,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 1000)
                    ) + expandVertically(
                        animationSpec = tween(durationMillis = 1000)
                    )
                ) {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier.width(180.dp)
                    ) {
                        Text("ПОВТОРИТЬ")
                    }
                }
            },
            dismissButton = {
                AnimatedVisibility(
                    visible = isButtonsVisible,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 1000)
                    ) + expandVertically(
                        animationSpec = tween(durationMillis = 1000)
                    )
                ) {
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.width(180.dp)
                    ) {
                        Text("ВЫЙТИ")
                    }
                }
            }
        )
    }
}

@Composable
fun TypewriterText(
    fullText: String,
    delayMillis: Long = 180,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    onFinished: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val typewriterPlayer = remember {
        MediaPlayer.create(context, R.raw.typewriter_ambient).apply {
            isLooping = true
            setVolume(0.3f, 0.3f)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (typewriterPlayer.isPlaying) {
                typewriterPlayer.stop()
            }
            typewriterPlayer.release()
        }
    }

    LaunchedEffect(fullText) {
        displayedText = ""
        typewriterPlayer.start()
        fullText.forEach { char ->
            displayedText += char
            delay(delayMillis)
        }

        onFinished()
        if (typewriterPlayer.isPlaying) {
            typewriterPlayer.stop()
        }
    }
    Text(text = displayedText, style = style, color = MaterialTheme.colorScheme.onSurfaceVariant)
}



@Composable
fun GameAudioManager(
    oxygenPercent: Int,
    gameStatus: GameStatus
) {
    val context = LocalContext.current

    val alertPlayer =
        remember { MediaPlayer.create(context, R.raw.alarm).apply { isLooping = true } }
    val lostPlayer =
        remember { MediaPlayer.create(context, R.raw.lost).apply { isLooping = false } }
    val wonPlayer = remember { MediaPlayer.create(context, R.raw.won).apply { isLooping = false } }
    val gamePlayer = remember { MediaPlayer.create(context, R.raw.game).apply { isLooping = true } }

    LaunchedEffect(oxygenPercent, gameStatus) {
        if (gameStatus == GameStatus.ACTIVE) {
            when (oxygenPercent) {
                in 1..20 -> {
                    if (gamePlayer.isPlaying) {
                        gamePlayer.pause()
                        gamePlayer.seekTo(0)
                    }
                    if (!alertPlayer.isPlaying) alertPlayer.start()
                }

                else -> {
                    if (alertPlayer.isPlaying) {
                        alertPlayer.pause()
                        alertPlayer.seekTo(0)
                    }
                    if (!gamePlayer.isPlaying) gamePlayer.start()
                }
            }
        } else {
            if (alertPlayer.isPlaying) alertPlayer.pause()
            if (gamePlayer.isPlaying) gamePlayer.pause()
        }
    }

    LaunchedEffect(gameStatus) {
        when (gameStatus) {
            GameStatus.LOST -> {
                wonPlayer.pause()
                lostPlayer.seekTo(0)
                lostPlayer.start()
            }

            GameStatus.WON -> {
                lostPlayer.pause()
                wonPlayer.seekTo(0)
                wonPlayer.start()
            }

            GameStatus.ACTIVE -> {
                if (lostPlayer.isPlaying) lostPlayer.pause()
                if (wonPlayer.isPlaying) wonPlayer.pause()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val players = listOf(alertPlayer, lostPlayer, wonPlayer, gamePlayer)
            players.forEach { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                } catch (e: Exception) {
                }
            }
        }
    }

}


//@Preview(showBackground = true, name = "Норма")
//@Composable
//fun OxygenNormalBlogPreview() {
//    val oxygen = 60
//    SpaceWaypointTheme {
//        OxygenBlog(
//            oxygen = oxygen,
//            oxygenStock = 10,
//            oxygenText = getOxygenTextRes(oxygen),
//            oxygenStockText = R.string.oxygen_stock,
//            onClick = { }
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Низкий")
//@Composable
//fun OxygenLowBlogPreview() {
//    val oxygen = 40
//    SpaceWaypointTheme {
//        OxygenBlog(
//            oxygen = oxygen,
//            oxygenStock = 10,
//            oxygenText = getOxygenTextRes(oxygen),
//            oxygenStockText = R.string.oxygen_stock,
//            onClick = { }
//        )
//    }
//}
//
@Preview(showBackground = true, name = "Критический")
@Composable
fun OxygenCriticalBlogPreview() {
    val oxygen = 20
    SpaceWaypointTheme {
        OxygenBlog(
            oxygen = oxygen,
            oxygenStock = (50..200).random(),
            oxygenText = getOxygenTextRes(oxygen),
            countOxygenUp = 5,
            oxygenStockText = R.string.oxygen_stock,
            onClick = { })
    }
}


@Preview
@Composable
fun TaskStatsBlockPreview() {
    SpaceWaypointTheme {
        TaskStatsBlock(
            40,
            1,
        )
    }
}

@Preview
@Composable
fun TaskItemPreviewTrue() {
    SpaceWaypointTheme {
        TaskItem(
            task = TaskList.getTasks()[0],
            taskNumber = 1,
            expanded = true,
            onToggle = {},
            onAnswerChecked = { },
            listState = rememberLazyListState(),
            coroutineScope = rememberCoroutineScope(),
        )
    }
}



