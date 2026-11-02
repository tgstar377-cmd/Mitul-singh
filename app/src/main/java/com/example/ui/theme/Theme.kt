package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = DeepBackground,
    primaryContainer = CyanPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = CyanLight,
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    secondaryContainer = ElectricIndigo.copy(alpha = 0.2f),
    onSecondaryContainer = Color.White,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = DeepBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
