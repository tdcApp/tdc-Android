package com.bagadbille.tdc.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bagadbille.tdc.navigation.BottomNavItem
import com.bagadbille.tdc.ui.home.HomeScreen
import com.bagadbille.tdc.ui.notifications.NotificationsScreen
import com.bagadbille.tdc.ui.profile.ProfileScreen

@Composable
fun MainScreen(
    onNavigateToClassDetail: (String) -> Unit,
    onNavigateToQuizTaking: (String) -> Unit,
    onNavigateToQuizResult: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = BottomNavItem.entries

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(if (selectedTab == index) item.selectedIcon else item.unselectedIcon, item.title) },
                        label = { Text(item.title) },
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
                0 -> ProfileScreen(onLogout = onLogout)
                1 -> HomeScreen(onNavigateToClassDetail, onNavigateToQuizTaking, onNavigateToQuizResult)
                2 -> NotificationsScreen()
            }
        }
    }
}
