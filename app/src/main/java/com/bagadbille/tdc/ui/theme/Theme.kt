package com.bagadbille.tdc.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = Color(0xFF070A12),
    primaryContainer = NeonEmeraldDark.copy(alpha = 0.25f),
    onPrimaryContainer = NeonEmeraldLight,
    secondary = NeonCyan,
    onSecondary = Color(0xFF070A12),
    secondaryContainer = NeonCyanDark.copy(alpha = 0.25f),
    onSecondaryContainer = Color.White,
    tertiary = NeonPurple,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSurfaceBorder,
    outlineVariant = GlassBorderSubtle,
    error = ErrorRed,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0D9488),          // Teal 600
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1), // Teal 100
    onPrimaryContainer = Color(0xFF0F766E),
    secondary = Color(0xFF0284C7),        // Sky 600
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF7C3AED),        // Purple 600
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),      // Slate 50
    onBackground = Color(0xFF0F172A),    // Slate 900
    surface = Color(0xFFFFFFFF),         // Clean white card surface
    onSurface = Color(0xFF0F172A),       // Slate 900
    surfaceVariant = Color(0xFFF1F5F9),  // Slate 100
    onSurfaceVariant = Color(0xFF475569),// Slate 600
    outline = Color(0xFFCBD5E1),         // Slate 300
    outlineVariant = Color(0xFFE2E8F0),  // Slate 200
    error = ErrorRed,
    onError = Color.White,
)

@Composable
fun TDCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TdcTypography,
        content = content
    )
}