package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RetroColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF04101A),
    primaryContainer = Color(0xFF0C3048),
    onPrimaryContainer = Color(0xFFC7F4FF),

    secondary = VioletSecondary,
    onSecondary = Color(0xFF1E0A38),
    secondaryContainer = Color(0xFF38155A),
    onSecondaryContainer = Color(0xFFF3E8FF),

    tertiary = AmberAccent,
    onTertiary = Color(0xFF261A00),
    tertiaryContainer = Color(0xFF4A3400),
    onTertiaryContainer = Color(0xFFFFECC7),

    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = CrimsonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // RetroForge defaults to high-contrast dark cyberpunk arcade theme
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = Typography,
        content = content
    )
}
