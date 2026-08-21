package com.bagadbille.tdc.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Main : Screen("main")
}

enum class BottomNavItem(
    val route: String, val title: String,
    val selectedIcon: ImageVector, val unselectedIcon: ImageVector
) {
    PROFILE("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person),
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NOTIFICATIONS("notifications", "Notifications", Icons.Filled.Notifications, Icons.Outlined.Notifications)
}
