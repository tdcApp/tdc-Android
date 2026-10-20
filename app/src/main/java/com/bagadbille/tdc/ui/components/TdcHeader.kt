package com.bagadbille.tdc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagadbille.tdc.ui.theme.CyberPrimaryGradient
import com.bagadbille.tdc.ui.theme.NeonEmerald
import com.bagadbille.tdc.ui.theme.NeonCyan

/**
 * Modern Cyber-styled App Header: Glowing emblem, gradient TDC branding, and frosted action buttons.
 */
@Composable
fun TdcHeader(
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnreadNotifications: Boolean = false,
    onRefreshClick: (() -> Unit)? = null,
    isRefreshing: Boolean = false
) {
    val isDark = MaterialTheme.colorScheme.background == com.bagadbille.tdc.ui.theme.DarkBackground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing Emblem
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = if (isDark) NeonEmerald.copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    if (isDark) {
                        Brush.radialGradient(
                            listOf(NeonEmerald.copy(alpha = 0.25f), Color(0xFF0F172A))
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(Color(0xFFCCFBF1), Color(0xFFF1F5F9))
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = "Logo",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.width(12.dp))

        // Center / Brand Title
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            val titleGradient = if (isDark) {
                CyberPrimaryGradient
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF0F766E), Color(0xFF0284C7)))
            }

            Text(
                text = "TDC",
                style = TextStyle(
                    brush = titleGradient,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = "TIT Developer Community",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Action Buttons: Refresh & Notifications in modern frosted circles
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onRefreshClick != null) {
                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.6f else 0.85f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(19.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.6f else 0.85f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(20.dp),
                        tint = if (hasUnreadNotifications) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasUnreadNotifications) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
