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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = BrandCyan,
    onPrimary = Color(0xFF071216),
    primaryContainer = BrandCyanDark.copy(alpha = 0.22f),
    onPrimaryContainer = BrandCyanLight,
    secondary = BrandOrange,
    onSecondary = Color(0xFF1F0E04),
    secondaryContainer = BrandOrangeDark.copy(alpha = 0.22f),
    onSecondaryContainer = BrandOrangeLight,
    tertiary = BrandCoral,
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
    primary = Color(0xFF009FB5),          // Deep Cyan for light contrast
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCF8FC),
    onPrimaryContainer = Color(0xFF006775),
    secondary = Color(0xFFEA7118),        // Deep Orange for light contrast
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8D6),
    onSecondaryContainer = Color(0xFF883B00),
    tertiary = BrandCoral,
    onTertiary = Color.White,
    background = LightBackground,         // #F5F5F7
    onBackground = LightTextPrimary,
    surface = LightSurface,               // #FFFFFF
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightSurfaceBorder,
    outlineVariant = Color(0xFFE8ECF2),
    error = ErrorRed,
    onError = Color.White,
)

private val TdcShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
        shapes = TdcShapes,
        content = content
    )
}