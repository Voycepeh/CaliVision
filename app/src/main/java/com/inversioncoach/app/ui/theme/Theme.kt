package com.inversioncoach.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = Color(0xFF67E8F9),
    onPrimary = Color(0xFF062B38),
    primaryContainer = Color(0xFF123E52),
    onPrimaryContainer = Color(0xFFE8FCFF),

    secondary = Color(0xFF7DD3FC),
    onSecondary = Color(0xFF082535),
    secondaryContainer = Color(0xFF103246),
    onSecondaryContainer = Color(0xFFEAF8FF),

    background = Color(0xFF020B16),
    onBackground = Color(0xFFF4FBFF),
    surface = Color(0xFF06111F),
    onSurface = Color(0xFFF4FBFF),
    surfaceVariant = Color(0xFF0D2234),
    onSurfaceVariant = Color(0xFFB8D4E3),

    outline = Color(0xFF3C5A6B),
    outlineVariant = Color(0xFF233847),

    error = Color(0xFFFF7A7A),
    onError = Color(0xFF2B0000)
)

@Composable
fun InversionCoachTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography(),
        content = content,
    )
}
