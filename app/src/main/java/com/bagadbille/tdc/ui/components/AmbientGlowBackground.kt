package com.bagadbille.tdc.ui.components

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * Signature Ambient Glow Background with illuminated Orange and Cyan neon ribbons / stripes.
 * Faithfully recreates the official TIT Developers branding with:
 * - Top-Right: Luminous curved warm orange wave ribbon with inner amber spine.
 * - Bottom-Left: Luminous fiery orange horizon dome.
 * - Bottom-Right: Flowing neon cyan wave ribbon with glowing crest.
 * - Top-Left: Deep teal atmospheric glow.
 */
@Composable
fun AmbientGlowBackground(
    modifier: Modifier = Modifier,
    enableAnimation: Boolean = true,
    subtle: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val alphaFactor = if (subtle) 0.42f else 1.0f

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow_transition")
    val pulse by if (enableAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ambient_pulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.5f) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient Blurred Glow Layer (Hardware Gaussian Blur on Android 12+, soft multi-stop radial gradients)
        val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier
                .fillMaxSize()
                .blur(radius = if (subtle) 64.dp else 48.dp)
        } else {
            Modifier.fillMaxSize()
        }

        Canvas(modifier = blurModifier) {
            val width = size.width
            val height = size.height

            // 1. Top-Left Cyan Ambient Field
            val cyanColor = if (isDark) Color(0xFF00B4D8) else Color(0xFF0284C7)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        cyanColor.copy(alpha = (if (isDark) 0.28f else 0.16f) * alphaFactor),
                        Color(0xFF023E8A).copy(alpha = (if (isDark) 0.14f else 0.08f) * alphaFactor),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.20f, height * 0.22f),
                    radius = width * 0.70f
                ),
                radius = width * 0.70f,
                center = Offset(width * 0.20f, height * 0.22f)
            )

            // 2. Top-Right Warm Orange/Amber Glow Field
            val orangeColor = if (isDark) Color(0xFFFF7A00) else Color(0xFFFF9E58)
            val orangeAlphaTop = (if (isDark) 0.45f + pulse * 0.08f else 0.24f) * alphaFactor
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFB366).copy(alpha = orangeAlphaTop),
                        orangeColor.copy(alpha = orangeAlphaTop * 0.75f),
                        Color(0xFFFF4800).copy(alpha = orangeAlphaTop * 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.88f + pulse * 10f, height * 0.15f),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(width * 0.88f + pulse * 10f, height * 0.15f)
            )

            // 3. Bottom-Left Vibrant Fiery Orange Dome
            val orangeAlphaBottom = (if (isDark) 0.65f + pulse * 0.08f else 0.35f) * alphaFactor
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF851B).copy(alpha = orangeAlphaBottom),
                        Color(0xFFFF4D00).copy(alpha = orangeAlphaBottom * 0.65f),
                        Color(0xFFE63300).copy(alpha = orangeAlphaBottom * 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.12f, height * 0.88f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.12f, height * 0.88f)
            )

            // 4. Bottom-Right Luminous Electric Cyan Field
            val cyanAlphaBottom = (if (isDark) 0.55f + pulse * 0.08f else 0.32f) * alphaFactor
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00F0FF).copy(alpha = cyanAlphaBottom),
                        Color(0xFF00B4D8).copy(alpha = cyanAlphaBottom * 0.65f),
                        Color(0xFF0077B6).copy(alpha = cyanAlphaBottom * 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.80f - pulse * 10f, height * 0.82f),
                    radius = width * 0.80f
                ),
                radius = width * 0.80f,
                center = Offset(width * 0.80f - pulse * 10f, height * 0.82f)
            )
        }

        // Frosted Glass Softening Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.Transparent,
                                Color(0x220B0B0D),
                                Color(0x440B0B0D)
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                Color(0x18F5F5F7),
                                Color(0x33F5F5F7)
                            )
                        }
                    )
                )
        )

        // Foreground content
        content()
    }
}
