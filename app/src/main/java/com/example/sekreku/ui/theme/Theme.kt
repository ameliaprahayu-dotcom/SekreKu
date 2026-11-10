package com.example.sekreku.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ======================= TEMA PINK =======================
private val LightColors = lightColorScheme(
    primary = Color(0xFFD81B60),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E3),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFFA14B6C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE1EC),
    onSecondaryContainer = Color(0xFF3A0B25),
    tertiary = Color(0xFFB4532A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBC9),
    onTertiaryContainer = Color(0xFF3A1200),
    background = Color(0xFFFFF8F9),
    onBackground = Color(0xFF22191B),
    surface = Color(0xFFFFF8F9),
    onSurface = Color(0xFF22191B),
    surfaceVariant = Color(0xFFF4DDE2),
    onSurfaceVariant = Color(0xFF524347),
    outline = Color(0xFF857377)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB1C8),
    onPrimary = Color(0xFF650033),
    primaryContainer = Color(0xFF8B1F4F),
    onPrimaryContainer = Color(0xFFFFD9E3),
    secondary = Color(0xFFE5B9CB),
    onSecondary = Color(0xFF43263A),
    secondaryContainer = Color(0xFF5C3C4F),
    onSecondaryContainer = Color(0xFFFFD8E8),
    tertiary = Color(0xFFFFB59C),
    onTertiary = Color(0xFF5B1A00),
    tertiaryContainer = Color(0xFF7F3815),
    onTertiaryContainer = Color(0xFFFFDBC9),
    background = Color(0xFF1F1A1B),
    onBackground = Color(0xFFEBE0E1),
    surface = Color(0xFF1F1A1B),
    onSurface = Color(0xFFEBE0E1),
    surfaceVariant = Color(0xFF524347),
    onSurfaceVariant = Color(0xFFD6C2C6),
    outline = Color(0xFF9F8C90)
)

@Composable
fun SekreKuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}