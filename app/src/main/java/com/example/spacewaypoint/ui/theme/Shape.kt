package com.example.spacewaypoint.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Для мелких элементов (кнопки, текстовые поля)
    small = RoundedCornerShape(4.dp),

    // Для средних элементов (карточки, диалоги, твой блок с кислородом)
    medium = RoundedCornerShape(16.dp),

    // Для крупных элементов (модальные окна на весь экран)
    large = RoundedCornerShape(24.dp)
)
