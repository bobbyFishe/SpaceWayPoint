package com.example.spacewaypoint.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spacewaypoint.R
import com.example.spacewaypoint.data.GameDifficulty

@Composable
fun ComplexityBlog(
    onDifficultySelected: (GameDifficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.outline),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp) // Отступ до кнопок
                .padding(horizontal = dimensionResource(R.dimen.padding_high)),
            horizontalArrangement = Arrangement.Center,
        ) {
            TypewriterText(
                fullText = stringResource(R.string.complexity_blog),
                style = MaterialTheme.typography.displayLarge
            )
        }
        Button(
            onClick = { onDifficultySelected(GameDifficulty.EASY) },
            modifier = Modifier.fillMaxWidth(0.7f).padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = ShapeDefaults.ExtraSmall
        ) {
            Text(text = stringResource(R.string.light_complexity))
        }

        Button(
            onClick = { onDifficultySelected(GameDifficulty.NORMAL) },
            modifier = Modifier.fillMaxWidth(0.7f).padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = ShapeDefaults.ExtraSmall
        ) {
            Text(text = stringResource(R.string.medium_comlexity))
        }

        Button(
            onClick = { onDifficultySelected(GameDifficulty.HARD) },
            modifier = Modifier.fillMaxWidth(0.7f).padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = ShapeDefaults.ExtraSmall
        ) {
            Text(text = stringResource(R.string.hard_complexity))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComplexityBlogPreview() {
    ComplexityBlog({})
}