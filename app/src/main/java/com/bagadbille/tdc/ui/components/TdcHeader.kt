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
import androidx.compose.foundation.text.selection.DisableSelection
import com.bagadbille.tdc.ui.theme.CyberPrimaryGradient
import com.bagadbille.tdc.ui.theme.NeonEmerald
import com.bagadbille.tdc.ui.theme.NeonCyan
import com.bagadbille.tdc.ui.theme.GlassBorderSubtle

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
        DisableSelection {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official Logo (Light theme uses tdc_logo_light with black middle slash)
                val logoRes = if (isDark) com.bagadbille.tdc.R.drawable.tdc_logo else com.bagadbille.tdc.R.drawable.tdc_logo_light
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = logoRes),
                    contentDescription = "TDC Logo",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(Modifier.width(12.dp))

                // Center / Brand Title
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    val titleColor = if (isDark) Color.White else Color.Black

                    Text(
                        text = "TDC",
                        style = TextStyle(
                            color = titleColor,
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
            }
        }

        // Action Buttons: Refresh & Notifications in modern elevated circles with visible outlines
        val actionBtnBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f) else Color.White
        val actionBtnBorder = if (isDark) GlassBorderSubtle else Color(0xFFCBD5E1)
        val actionBtnIconTint = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF334155)

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
                        .background(actionBtnBg)
                        .border(if (isDark) 1.dp else 1.2.dp, actionBtnBorder, CircleShape)
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
                            tint = actionBtnIconTint
                        )
                    }
                }
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(actionBtnBg)
                    .border(if (isDark) 1.dp else 1.2.dp, actionBtnBorder, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(20.dp),
                        tint = if (hasUnreadNotifications) MaterialTheme.colorScheme.primary else actionBtnIconTint
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
