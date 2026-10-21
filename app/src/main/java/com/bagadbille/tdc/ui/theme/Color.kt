package com.bagadbille.tdc.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── TIT Developers Design System Palette ───

// Core Brand Colors
val BrandCyan = Color(0xFF55D6E8)           // Primary Brand Cyan
val BrandCyanDark = Color(0xFF1CB5CB)
val BrandCyanLight = Color(0xFF8DE8F3)

val BrandOrange = Color(0xFFFF8A32)         // Secondary Brand Orange
val BrandOrangeDark = Color(0xFFE06E1A)
val BrandOrangeLight = Color(0xFFFFB378)

val BrandCoral = Color(0xFFF46B66)          // Accent Coral / Rose

// Backward-compatible aliases for existing screens
val NeonEmerald = BrandCyan
val NeonEmeraldDark = BrandCyanDark
val NeonEmeraldLight = BrandCyanLight
val NeonCyan = BrandCyan
val NeonCyanDark = BrandCyanDark
val NeonPurple = BrandOrange
val NeonPurpleDark = BrandOrangeDark
val NeonAmber = BrandOrange
val NeonRose = BrandCoral

val TealPrimary = BrandCyan
val TealPrimaryDark = BrandCyanDark
val TealPrimaryLight = BrandCyanLight
val BlueSecondary = BrandOrange
val BlueSecondaryDark = BrandOrangeDark

// ─── Dark Theme Surfaces ───
val DarkBackground = Color(0xFF0B0B0D)       // Dark Background #0B0B0D
val DarkSurface = Color(0xFF17191E)          // Dark Surface #17191E
val DarkSurfaceVariant = Color(0xFF20242A)   // Dark Surface Variant
val DarkSurfaceBorder = Color(0xFF2D333E)    // Subtle Card Border

// Glassmorphism overlays
val GlassBackground = Color(0xD917191E)
val GlassSurface = Color(0xEB20242A)
val GlassBorderSubtle = Color(0x1FFFFFFF)
val GlassBorderNeon = Color(0x3355D6E8)

// Light Theme Surfaces
val LightBackground = Color(0xFFF5F5F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEDEFF2)
val LightSurfaceBorder = Color(0xFFE2E5EB)

// Typography Text Colors
val DarkTextPrimary = Color(0xFFF5F5F7)     // #F5F5F7
val DarkTextSecondary = Color(0xFFA5ABB5)   // #A5ABB5
val DarkTextMuted = Color(0xFF7A8290)
val LightTextPrimary = Color(0xFF0B0B0D)
val LightTextSecondary = Color(0xFF5A6270)
val LightTextMuted = Color(0xFF8B93A0)

// Status Colors
val ErrorRed = Color(0xFFF46B66)            // Accent Coral
val SuccessGreen = Color(0xFF34D399)        // Mint Emerald
val WarningAmber = Color(0xFFFF8A32)        // Brand Orange

// ─── Branded Gradients ───
val CyberPrimaryGradient = Brush.horizontalGradient(
    listOf(BrandCyan, Color(0xFF22B8CF))
)

val CyberOrangeGradient = Brush.horizontalGradient(
    listOf(BrandOrange, BrandCoral)
)

val CyberPurpleGradient = CyberOrangeGradient

val CyberGlassCardGradient = Brush.verticalGradient(
    listOf(Color(0xD91A1E27), Color(0xBF12151D))
)

val FrostedGlassBarGradient = Brush.verticalGradient(
    listOf(Color(0xD9171C26), Color(0xEB0E121A))
)

val CyberHeroGradient = Brush.linearGradient(
    listOf(Color(0xFF0E252D), Color(0xFF1E1712))
)