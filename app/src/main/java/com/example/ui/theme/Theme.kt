package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PureBlackColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = SurfaceHover,
    onPrimaryContainer = TextPrimary,
    secondary = Color.White,
    onSecondary = Color.Black,
    tertiary = AccentSuccess,
    onTertiary = Color.White,
    background = AppBg,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceInput,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor
)

@Composable
fun MesgagingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PureBlackColorScheme,
        typography = Typography,
        content = content
    )
}
