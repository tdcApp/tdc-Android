package com.bagadbille.tdc.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.navigation.BottomNavItem
import com.bagadbille.tdc.ui.assignments.AssignmentsScreen
import com.bagadbille.tdc.ui.components.AmbientGlowBackground
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.home.HomeScreen
import com.bagadbille.tdc.ui.mentor.assignments.MentorAssignmentsScreen
import com.bagadbille.tdc.ui.mentor.dashboard.MentorDashboardScreen
import com.bagadbille.tdc.ui.profile.ProfileScreen
import com.bagadbille.tdc.ui.theme.*

@Composable
fun MainScreen(
    onNavigateToQuizTaking: (String) -> Unit,
    onNavigateToQuizResult: (String) -> Unit,
    onNavigateToAssignmentDetail: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCreateAssignment: () -> Unit,
    onNavigateToAssignmentSubmissions: (String) -> Unit,
    onNavigateToCreateQuiz: () -> Unit,
    onNavigateToQuizResults: (String) -> Unit,
    onLogout: () -> Unit,
    targetSection: String? = null,
    onTargetSectionConsumed: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isMentor by viewModel.isMentor.collectAsStateWithLifecycle()
    val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var homeTargetPage by remember { mutableStateOf<Int?>(null) }
    var refreshQuizSignal by remember { mutableLongStateOf(0L) }
    var refreshAssignmentSignal by remember { mutableLongStateOf(0L) }

    LaunchedEffect(targetSection) {
        targetSection?.let { section ->
            when (section.uppercase()) {
                "QUIZ" -> {
                    selectedTab = 0
                    homeTargetPage = 2
                    refreshQuizSignal = System.currentTimeMillis()
                }
                "ASSIGNMENT" -> {
                    selectedTab = 1
                    refreshAssignmentSignal = System.currentTimeMillis()
                }
            }
            onTargetSectionConsumed()
        }
    }

    val tabs = BottomNavItem.entries

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    // Request Notification permission on Android 13+ (API 33+) after login
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* permission result handled by OS */ }

        LaunchedEffect(Unit) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Wait for the stored role so the wrong side never flashes
    val mentor = isMentor ?: run {
        LoadingScreen()
        return
    }

    AmbientGlowBackground(subtle = true) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                FrostedGlassBottomBar(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    isMentor = mentor
                )
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> if (mentor) {
                        MentorDashboardScreen(
                            onNotificationClick = onNavigateToNotifications,
                            onCreateAssignment = onNavigateToCreateAssignment,
                            onCreateQuiz = onNavigateToCreateQuiz,
                            onOpenQuizResults = onNavigateToQuizResults,
                            onNavigateToAssignmentsTab = { selectedTab = 1 },
                            hasUnreadNotifications = hasUnreadNotifications
                        )
                    } else {
                        HomeScreen(
                            onNavigateToQuizTaking = onNavigateToQuizTaking,
                            onNavigateToQuizResult = onNavigateToQuizResult,
                            onNavigateToAssignments = { selectedTab = 1 },
                            onNotificationClick = onNavigateToNotifications,
                            hasUnreadNotifications = hasUnreadNotifications,
                            targetPage = homeTargetPage,
                            onTargetPageConsumed = { homeTargetPage = null },
                            refreshQuizSignal = refreshQuizSignal
                        )
                    }
                    1 -> if (mentor) {
                        MentorAssignmentsScreen(
                            onCreateAssignment = onNavigateToCreateAssignment,
                            onOpenAssignment = onNavigateToAssignmentSubmissions,
                            onNotificationClick = onNavigateToNotifications,
                            hasUnreadNotifications = hasUnreadNotifications
                        )
                    } else {
                        AssignmentsScreen(
                            onNavigateToAssignmentDetail = onNavigateToAssignmentDetail,
                            onNotificationClick = onNavigateToNotifications,
                            hasUnreadNotifications = hasUnreadNotifications,
                            refreshSignal = refreshAssignmentSignal
                        )
                    }
                    2 -> ProfileScreen(
                        onLogout = onLogout,
                        onNotificationClick = onNavigateToNotifications,
                        hasUnreadNotifications = hasUnreadNotifications
                    )
                }
            }
        }
    }
}

/**
 * Frosted Glass Floating Bottom Navigation Bar with specular glass highlight,
 * translucent dark backdrop, and animated luminous active state.
 */
@Composable
private fun FrostedGlassBottomBar(
    tabs: List<BottomNavItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isMentor: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val activeColor = if (isDark) BrandCyan else MaterialTheme.colorScheme.primary
    val inactiveColor = if (isDark) Color(0xFF8B93A0) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = if (isDark) Color(0xD9131620) else Color(0xF2FFFFFF), // Translucent frosted glass adapted for theme
            border = if (isDark) {
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.22f), // Specular rim reflection
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
            } else {
                BorderStroke(1.dp, Color(0xFFE2E8F0))
            },
            shadowElevation = if (isDark) 12.dp else 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    val isDashboard = isMentor && item == BottomNavItem.HOME
                    val title = if (isDashboard) "Dashboard" else item.title
                    val icon = when {
                        isDashboard && isSelected -> Icons.Filled.Dashboard
                        isDashboard -> Icons.Outlined.Dashboard
                        isSelected -> item.selectedIcon
                        else -> item.unselectedIcon
                    }

                    val pillAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(durationMillis = 220),
                        label = "pillAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        // Glowing Pill Capsule for selected tab
                        if (pillAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                activeColor.copy(alpha = (if (isDark) 0.16f else 0.12f) * pillAlpha),
                                                (if (isDark) BrandOrange else MaterialTheme.colorScheme.secondary).copy(alpha = (if (isDark) 0.10f else 0.08f) * pillAlpha)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 0.8.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                activeColor.copy(alpha = (if (isDark) 0.40f else 0.35f) * pillAlpha),
                                                (if (isDark) BrandOrange else MaterialTheme.colorScheme.secondary).copy(alpha = 0.25f * pillAlpha)
                                            )
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            )
                        }

                        // Icon + Label Stack
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) activeColor else inactiveColor
                            )
                        }
                    }
                }
            }
        }
    }
}
