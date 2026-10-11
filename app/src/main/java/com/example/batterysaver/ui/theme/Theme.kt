package com.example.batterysaver.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BgDark,
    primaryContainer = Surface2Dark,
    onPrimaryContainer = TextDark,
    secondary = GoodGreen,
    onSecondary = BgDark,
    background = BgDark,
    onBackground = TextDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = Surface2Dark,
    onSurfaceVariant = Text2Dark,
    outline = Line2Dark,
    error = BadRed
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlueLight,
    onPrimary = SurfaceLight,
    primaryContainer = Surface2Light,
    onPrimaryContainer = TextLight,
    secondary = GoodGreen,
    onSecondary = SurfaceLight,
    background = BgLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = Surface2Light,
    onSurfaceVariant = Text2Light,
    outline = Line2Light,
    error = BadRed
)

@Composable
fun BatterySaverTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
