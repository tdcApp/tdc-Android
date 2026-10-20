package com.bagadbille.tdc.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Cyber Dark & Neon Palette (Linear / Arc Inspired) ───

// Neon Primary — Electric Mint / Emerald
val NeonEmerald = Color(0xFF00F5A0)
val NeonEmeraldDark = Color(0xFF00B87A)
val NeonEmeraldLight = Color(0xFF6EFCBF)

// Neon Secondary — Cyber Cyan
val NeonCyan = Color(0xFF00D9F5)
val NeonCyanDark = Color(0xFF009BB0)

// Neon Accents
val NeonPurple = Color(0xFFA855F7)
val NeonPurpleDark = Color(0xFF7C3AED)
val NeonAmber = Color(0xFFFBBF24)
val NeonRose = Color(0xFFFB7185)

// Backward-compatible aliases for existing screens
val TealPrimary = NeonEmerald
val TealPrimaryDark = NeonEmeraldDark
val TealPrimaryLight = NeonEmeraldLight
val BlueSecondary = NeonCyan
val BlueSecondaryDark = NeonCyanDark

// ─── Deep Midnight OLED Surfaces ───
val DarkBackground = Color(0xFF070A12)       // Ultra-deep space black
val DarkSurface = Color(0xFF0F1523)          // Cyber base surface
val DarkSurfaceVariant = Color(0xFF161F33)   // Elevated glass card surface
val DarkSurfaceBorder = Color(0xFF222E47)    // Crisp subtle border

// Glassmorphism overlays
val GlassBackground = Color(0xB30F1523)     // 70% opacity frosted glass
val GlassSurface = Color(0xCC161F33)        // 80% opacity card glass
val GlassBorderSubtle = Color(0x1AFFFFFF)   // 10% white micro-edge
val GlassBorderNeon = Color(0x3300F5A0)     // Translucent neon glow stroke

// Light Theme Surfaces (Clean Ice Slate)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)

// Text
val DarkTextPrimary = Color(0xFFF8FAFC)     // Slate 50 — ultra crisp
val DarkTextSecondary = Color(0xFF94A3B8)   // Slate 400 — smooth readable
val DarkTextMuted = Color(0xFF64748B)       // Slate 500 — subtle metadata
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

// Status
val ErrorRed = Color(0xFFF43F5E)            // Rose 500
val SuccessGreen = Color(0xFF10B981)        // Emerald 500
val WarningAmber = Color(0xFFF59E0B)        // Amber 500

// ─── Reusable Cyber Gradients ───
val CyberPrimaryGradient = Brush.horizontalGradient(
    listOf(NeonEmerald, NeonCyan)
)

val CyberPurpleGradient = Brush.horizontalGradient(
    listOf(NeonPurple, Color(0xFF6366F1))
)

val CyberGlassCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF151D2F), Color(0xFF0A0F1D))
)

val CyberHeroGradient = Brush.linearGradient(
    listOf(Color(0xFF063327), Color(0xFF0A1224))
)