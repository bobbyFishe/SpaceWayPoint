package com.example.spacewaypoint.ui.theme

import android.R.attr.fontFamily
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.spacewaypoint.R

val orbitron = FontFamily(
    Font(R.font.orbitron_regular)
)

val techMono = FontFamily(
    Font(R.font.share_tech_mono_regular)
)

val exo2 = FontFamily(
    Font(R.font.exo2_medium)
)

val barlow = FontFamily(
    Font(R.font.barlow_condensed_extra_light)
)

val russo_one = FontFamily(
    Font(R.font.russo_one_regular)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = russo_one,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.1.sp,
        fontSize = 18.sp
    ),
    displayMedium = TextStyle(
        fontFamily = exo2,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 2.sp
    ),
    displaySmall = TextStyle(
        fontFamily = techMono,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = techMono,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = exo2,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

)