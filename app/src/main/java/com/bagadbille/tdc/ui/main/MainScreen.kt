package com.bagadbille.tdc.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.navigation.BottomNavItem
import com.bagadbille.tdc.ui.assignments.AssignmentsScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.home.HomeScreen
import com.bagadbille.tdc.ui.mentor.assignments.MentorAssignmentsScreen
import com.bagadbille.tdc.ui.mentor.dashboard.MentorDashboardScreen
import com.bagadbille.tdc.ui.profile.ProfileScreen

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
    viewModel: MainViewModel = hiltViewModel()
) {
    val isMentor by viewModel.isMentor.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(1) }
    val tabs = BottomNavItem.entries

    // Wait for the stored role so the wrong side never flashes
    val mentor = isMentor ?: run {
        LoadingScreen()
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, item ->
                    val isDashboard = mentor && item == BottomNavItem.HOME
                    val title = if (isDashboard) "Dashboard" else item.title
                    val icon = when {
                        isDashboard && selectedTab == index -> Icons.Filled.Dashboard
                        isDashboard -> Icons.Outlined.Dashboard
                        selectedTab == index -> item.selectedIcon
                        else -> item.unselectedIcon
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ProfileScreen(
                    onLogout = onLogout,
                    onNotificationClick = onNavigateToNotifications
                )
                1 -> if (mentor) {
                    MentorDashboardScreen(
                        onNotificationClick = onNavigateToNotifications,
                        onCreateAssignment = onNavigateToCreateAssignment,
                        onCreateQuiz = onNavigateToCreateQuiz,
                        onOpenQuizResults = onNavigateToQuizResults
                    )
                } else {
                    HomeScreen(
                        onNavigateToQuizTaking = onNavigateToQuizTaking,
                        onNavigateToQuizResult = onNavigateToQuizResult,
                        onNotificationClick = onNavigateToNotifications
                    )
                }
                2 -> if (mentor) {
                    MentorAssignmentsScreen(
                        onCreateAssignment = onNavigateToCreateAssignment,
                        onOpenAssignment = onNavigateToAssignmentSubmissions,
                        onNotificationClick = onNavigateToNotifications
                    )
                } else {
                    AssignmentsScreen(
                        onNavigateToAssignmentDetail = onNavigateToAssignmentDetail,
                        onNotificationClick = onNavigateToNotifications
                    )
                }
            }
        }
    }
}
