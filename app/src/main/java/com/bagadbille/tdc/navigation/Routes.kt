package com.bagadbille.tdc.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Setup : Screen("setup")
    data object Main : Screen("main")
    data object Notifications : Screen("notifications")
    data object AssignmentDetail : Screen("assignmentDetail/{assignmentId}") {
        fun createRoute(assignmentId: String) = "assignmentDetail/$assignmentId"
    }
    data object AssignmentEditor : Screen("assignmentEditor")
    data object AssignmentSubmissions : Screen("assignmentSubmissions/{assignmentId}") {
        fun createRoute(assignmentId: String) = "assignmentSubmissions/$assignmentId"
    }
    data object CreateQuiz : Screen("createQuiz")
    data object QuizResults : Screen("quizResults/{quizId}") {
        fun createRoute(quizId: String) = "quizResults/$quizId"
    }
}

enum class BottomNavItem(
    val route: String, val title: String,
    val selectedIcon: ImageVector, val unselectedIcon: ImageVector
) {
    PROFILE("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person),
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    ASSIGNMENTS("assignments", "Assignments", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment)
}
