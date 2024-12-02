// theme/Theme.kt
package com.example.directedsonarapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

private val DarkColorPalette = darkColors(
    primary = Purple80,
    secondary = PurpleGrey80,
    onPrimary = Pink80
)

private val LightColorPalette = lightColors(
    primary = Purple40,
    secondary = PurpleGrey40,
    onPrimary = Pink40

    /* Другие цвета, если нужно
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun DirectedSonarAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
