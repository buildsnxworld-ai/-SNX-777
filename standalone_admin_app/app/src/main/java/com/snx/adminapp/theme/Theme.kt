package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SnxDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = CasinoBg,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = AccentCyan,
    onSecondary = CasinoBg,
    tertiary = AccentCrimson,
    onTertiary = TextPrimary,
    background = CasinoBg,
    onBackground = TextPrimary,
    surface = CasinoSurface,
    onSurface = TextPrimary,
    surfaceVariant = CasinoSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CasinoCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SnxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
