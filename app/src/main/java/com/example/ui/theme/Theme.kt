package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ps5DarkColorScheme = darkColorScheme(
    primary = Ps5Blue,
    onPrimary = Color.White,
    secondary = Ps5Cyan,
    onSecondary = Color.White,
    tertiary = Ps5Glow,
    background = Ps5Background,
    onBackground = Ps5TextPrimary,
    surface = Ps5Surface,
    onSurface = Ps5TextPrimary,
    surfaceVariant = Ps5SurfaceVariant,
    onSurfaceVariant = Ps5TextSecondary,
    outline = Ps5CardBorder,
)

@Composable
fun RobotArmTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Ps5DarkColorScheme,
        typography = Typography,
        content = content
    )
}
