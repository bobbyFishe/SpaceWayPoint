package com.example.spacewaypoint.ui
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spacewaypoint.R
import kotlinx.coroutines.delay

data class ComicStep(val text: String, val color: Color)

@Composable
fun ComicIntroScreen(onNextScreen: () -> Unit) {
    val steps = listOf(
        ComicStep("СИСТЕМА АКТИВИРОВАНА. ПОИСК ПОЛЬЗОВАТЕЛЯ...", Color(0xFF00FF41)),
        ComicStep("ОБНАРУЖЕН КРИТИЧЕСКИЙ УРОВЕНЬ O2. КАПСУЛА №404 ЗАГЕРМЕТИЗИРОВАНА.", Color(0xFFFF4444)),
        ComicStep("Я — АРГОС. ТВОЙ ГОЛОС — ЕДИНСТВЕННЫЙ ШАНС НА РАЗБЛОКИРОВКУ ФИЛЬТРОВ.", Color(0xFF00FF41)),
        ComicStep("ПОМНИШЬ ЛИ ТЫ КОДЫ ДОСТУПА? БЕЗ НИХ ТЫ НЕ СДЕЛАЕШЬ СЛЕДУЮЩИЙ ВДОХ.", Color.White),
        ComicStep("АНАЛИЗ ДАННЫХ — ЕДИНСТВЕННЫЙ СПОСОБ ВЫЖИТЬ.", Color(0xFFFF4444)),
        ComicStep("НАЧИНАЕМ СИНХРОНИЗАЦИЮ...", Color(0xFF00FF41))
    )
    val images: List<Int> = listOf(
        R.drawable.image1,
        R.drawable.image2,
        R.drawable.image3,
        R.drawable.image4,
        R.drawable.image5,
        R.drawable.image6
    )

    var visibleStepsCount by remember { mutableIntStateOf(0) }
    var showButton by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        for (i in 1..steps.size) {
            delay(1800)
            visibleStepsCount = i
        }
        delay(2000)
        showButton = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.weight(1f)) {
            steps.chunked(2).forEachIndexed { rowIndex, rowSteps ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSteps.forEachIndexed { columnIndex, step ->
                        val overallIndex = rowIndex * 2 + columnIndex
                        Box(modifier = Modifier.weight(1f)) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = visibleStepsCount > overallIndex,
                                enter = fadeIn() + scaleIn()
                            ) {
                                ComicCard(
                                    step = step,
                                    image = images[overallIndex]
                                )
                            }
                        }
                    }
                    if (rowSteps.size < 2) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .heightIn(min = 80.dp) // Минимальная высота для области кнопки
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            if (showButton) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(1000)),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNextScreen,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .padding(bottom = 32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF41)),
                        shape = ShapeDefaults.ExtraSmall
                    ) {
                        Text(
                            "Аутентификация",
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ComicCard(
    step: ComicStep,
    image: Int
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = ShapeDefaults.ExtraSmall
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = step.text,
                color = step.color,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp,
            )
        }
    }
}


@Preview(showBackground = true, name = "Полный сюжет")
@Composable
fun FullComicPreview() {
    MaterialTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))) {
            ComicIntroScreen(onNextScreen = {})
        }
    }
}

@Preview(name = "Дизайн карточки")
@Composable
fun SingleCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ComicCard(
                step = ComicStep(
                    text = "ВНИМАНИЕ: ОБНАРУЖЕНА УТЕЧКА КИСЛОРОДА!",
                    color = Color(0xFFFF4444)
                ),
                image = R.drawable.image1
            )
        }
    }
}