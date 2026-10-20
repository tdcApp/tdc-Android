package com.bagadbille.tdc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bagadbille.tdc.ui.theme.CyberGlassCardGradient
import com.bagadbille.tdc.ui.theme.DarkBackground
import com.bagadbille.tdc.ui.theme.GlassBorderSubtle

/**
 * Responsive Card container:
 * - In Dark Mode: Signature Cyber glassmorphism with dark gradient and subtle translucent border.
 * - In Light Mode: Clean, elevated crisp white surface with soft modern shadow and slate micro-border.
 */
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    borderBrush: Brush? = null,
    backgroundBrush: Brush? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == com.bagadbille.tdc.ui.theme.DarkSurface

    val resolvedBorder = if (borderBrush != null) {
        BorderStroke(borderWidth, borderBrush)
    } else {
        val color = if (isDark) {
            borderColor ?: Color.White.copy(alpha = 0.08f)
        } else {
            when {
                borderColor == null || borderColor == GlassBorderSubtle || borderColor == Color.White.copy(alpha = 0.08f) ->
                    Color(0xFFE2E8F0) // Clean slate border in light mode
                borderColor == com.bagadbille.tdc.ui.theme.NeonEmerald || borderColor.copy(alpha = 1f) == com.bagadbille.tdc.ui.theme.NeonEmerald ->
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                borderColor == com.bagadbille.tdc.ui.theme.NeonCyan || borderColor.copy(alpha = 1f) == com.bagadbille.tdc.ui.theme.NeonCyan ->
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                else ->
                    borderColor.copy(alpha = 0.4f)
            }
        }
        BorderStroke(borderWidth, color)
    }

    val shadowModifier = if (!isDark) {
        Modifier.shadow(
            elevation = 3.dp,
            shape = shape,
            clip = false,
            ambientColor = Color(0x100F172A),
            spotColor = Color(0x140F172A)
        )
    } else {
        Modifier
    }

    val backgroundModifier = when {
        backgroundColor != null -> Modifier.background(backgroundColor, shape)
        backgroundBrush != null -> Modifier.background(backgroundBrush, shape)
        isDark -> Modifier.background(CyberGlassCardGradient, shape)
        else -> Modifier.background(Color.White, shape)
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .border(resolvedBorder, shape)
            .then(backgroundModifier)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        content = content
    )
}
