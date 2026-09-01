package com.bagadbille.tdc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bagadbille.tdc.ui.assignments.AssignmentDetailScreen
import com.bagadbille.tdc.ui.auth.LoginScreen
import com.bagadbille.tdc.ui.auth.SignupScreen
import com.bagadbille.tdc.ui.auth.SplashScreen
import com.bagadbille.tdc.ui.home.classes.ClassDetailScreen
import com.bagadbille.tdc.ui.home.quiz.QuizResultScreen
import com.bagadbille.tdc.ui.home.quiz.QuizTakingScreen
import com.bagadbille.tdc.ui.main.MainScreen
import com.bagadbille.tdc.ui.notifications.NotificationsScreen

@Composable
fun TdcNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToMain = { navController.navigate(Screen.Main.route) { popUpTo(Screen.Splash.route) { inclusive = true } } }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateToMain = { navController.navigate(Screen.Main.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToMain = { navController.navigate(Screen.Main.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToClassDetail = { navController.navigate("classDetail/$it") },
                onNavigateToQuizTaking = { navController.navigate("quizTaking/$it") },
                onNavigateToQuizResult = { navController.navigate("quizResult/$it") },
                onNavigateToAssignmentDetail = { navController.navigate(Screen.AssignmentDetail.createRoute(it)) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Main.route) { inclusive = true } } }
            )
        }
        composable("classDetail/{classId}", arguments = listOf(navArgument("classId") { type = NavType.StringType })) {
            ClassDetailScreen(classId = it.arguments?.getString("classId") ?: "", onNavigateBack = { navController.popBackStack() })
        }
        composable("quizTaking/{quizId}", arguments = listOf(navArgument("quizId") { type = NavType.StringType })) {
            val qId = it.arguments?.getString("quizId") ?: ""
            QuizTakingScreen(quizId = qId, onNavigateBack = { navController.popBackStack() },
                onQuizSubmitted = { navController.navigate("quizResult/$qId") { popUpTo("quizTaking/$qId") { inclusive = true } } })
        }
        composable("quizResult/{quizId}", arguments = listOf(navArgument("quizId") { type = NavType.StringType })) {
            QuizResultScreen(quizId = it.arguments?.getString("quizId") ?: "", onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            Screen.AssignmentDetail.route,
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
        ) {
            AssignmentDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

