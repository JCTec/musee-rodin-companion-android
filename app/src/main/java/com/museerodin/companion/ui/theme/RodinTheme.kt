package com.museerodin.companion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bronze = Color(0xFF7B5427)
val Patina = Color(0xFF1F7A68)
val Plum = Color(0xFF704565)
val Paper = Color(0xFFF8F5EF)
val Ink = Color(0xFF231F20)
val Night = Color(0xFF16191D)
val NightCard = Color(0xFF24292F)

private val LightColors = lightColorScheme(
    primary = Bronze,
    onPrimary = Color.White,
    secondary = Patina,
    tertiary = Plum,
    background = Paper,
    onBackground = Ink,
    surface = Color(0xFFFFFBF7),
    onSurface = Ink,
    surfaceVariant = Color(0xFFE7DFD4),
    onSurfaceVariant = Color(0xFF514B45),
    outline = Color(0xFF82766A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE3B271),
    onPrimary = Color(0xFF442B0B),
    secondary = Color(0xFF80D8C5),
    tertiary = Color(0xFFE2B7D7),
    background = Night,
    onBackground = Color(0xFFF2ECE4),
    surface = NightCard,
    onSurface = Color(0xFFF2ECE4),
    surfaceVariant = Color(0xFF3A3F45),
    onSurfaceVariant = Color(0xFFD0C8BF),
    outline = Color(0xFFA89D91),
)

@Composable
fun RodinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}

