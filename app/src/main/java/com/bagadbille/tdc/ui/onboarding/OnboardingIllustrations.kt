package com.bagadbille.tdc.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Slide 1 Illustration: Glowing Glass Code Editor Window + Floating Badges
 */
@Composable
fun LearnTogetherIllustration(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val infiniteTransition = rememberInfiniteTransition(label = "code_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow behind card
        Box(
            modifier = Modifier
                .size(190.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00D2EE).copy(alpha = if (isDark) 0.35f else 0.22f),
                            Color(0xFFFF7A00).copy(alpha = if (isDark) 0.20f else 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Glassmorphism Code Editor Window
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 150.dp)
                .offset(y = floatOffset.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isDark) Color(0xCC181C24) else Color(0xE6FFFFFF)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF55D6E8).copy(alpha = 0.6f),
                            Color(0xFFFF8A32).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Window dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF46B66)))
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFB74D)))
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF55D6E8)))
                }

                Spacer(Modifier.weight(1f))

                // Big Glowing Code Symbol </>
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "</>",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFF8A32),
                        letterSpacing = 2.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                // Mini code line skeletons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF55D6E8).copy(alpha = 0.6f))
                    )
                    Box(
                        Modifier
                            .width(70.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                }
            }
        }

        // Floating user group chip on bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-12 + floatOffset * -0.8f).dp)
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDark) Color(0xE61F2530) else Color(0xF2FFFFFF))
                .border(
                    width = 1.dp,
                    color = Color(0xFF55D6E8).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                tint = Color(0xFF55D6E8),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Slide 2 Illustration: Glowing Community Avatars + Floating Book & Chat Bubbles
 */
@Composable
fun ConnectWithDevelopersIllustration(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val infiniteTransition = rememberInfiniteTransition(label = "community_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Radial ambient glow
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF8A32).copy(alpha = if (isDark) 0.32f else 0.20f),
                            Color(0xFF00D2EE).copy(alpha = if (isDark) 0.22f else 0.14f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Floating Book Badge (Top-Left)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 24.dp, y = (26 + floatOffset * 0.9f).dp)
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xCC181C24) else Color(0xE6FFFFFF))
                .border(1.dp, Color(0xFF55D6E8).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = Color(0xFF55D6E8),
                modifier = Modifier.size(22.dp)
            )
        }

        // Floating Chat Bubble Badge (Top-Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-26).dp, y = (28 - floatOffset * 0.9f).dp)
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDark) Color(0xCC181C24) else Color(0xE6FFFFFF))
                .border(1.dp, Color(0xFFFF8A32).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Chat,
                contentDescription = null,
                tint = Color(0xFFFF8A32),
                modifier = Modifier.size(22.dp)
            )
        }

        // Central Group of Stylized Glowing Avatars
        Row(
            modifier = Modifier
                .offset(y = (20 + floatOffset).dp),
            horizontalArrangement = Arrangement.spacedBy((-10).dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Avatar (Coral/Orange)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 12.dp)
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF46B66))
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .size(width = 44.dp, height = 30.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFF46B66), Color(0xFFFF8A32))
                            )
                        )
                )
            }

            // Center Avatar (Hero Vibrant Cyan)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 0.dp)
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF55D6E8), Color(0xFF0077B6))
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .size(width = 58.dp, height = 40.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF55D6E8), Color(0xFF005F99))
                            )
                        )
                )
            }

            // Right Avatar (Warm Amber)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 12.dp)
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF8A32))
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .size(width = 44.dp, height = 30.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFF8A32), Color(0xFFE05300))
                            )
                        )
                )
            }
        }
    }
}

/**
 * Slide 3 Illustration: Sleek Rocket Launching with Fiery Energy Trail
 */
@Composable
fun GrowYourCareerIllustration(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val infiniteTransition = rememberInfiniteTransition(label = "rocket_pulse")
    val thrust by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thrust"
    )

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Warm Orange & Cyan Exhaust Cloud Glow
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-20).dp, y = 20.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF7A00).copy(alpha = if (isDark) 0.38f else 0.25f),
                            Color(0xFF55D6E8).copy(alpha = if (isDark) 0.22f else 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Rocket Capsule Container (Angled Launch)
        Box(
            modifier = Modifier
                .offset(x = (thrust * 0.8f).dp, y = (-thrust * 0.8f).dp)
                .rotate(45f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Rocket Icon
                Icon(
                    imageVector = Icons.Outlined.RocketLaunch,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFFF5F5F7) else Color(0xFF17191E),
                    modifier = Modifier.size(86.dp)
                )

                // Fiery Thruster Trail
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 34.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFF8A32),
                                    Color(0xFFF46B66),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // Tiny floating accent sparkle badges
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-36).dp, y = 28.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF55D6E8))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 36.dp, y = (-32).dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF8A32))
        )
    }
}
