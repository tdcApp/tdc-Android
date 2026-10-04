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
import com.bagadbille.tdc.ui.auth.SetupScreen
import com.bagadbille.tdc.ui.auth.SplashScreen
import com.bagadbille.tdc.ui.home.quiz.QuizResultScreen
import com.bagadbille.tdc.ui.home.quiz.QuizTakingScreen
import com.bagadbille.tdc.ui.main.MainScreen
import com.bagadbille.tdc.ui.mentor.assignments.AssignmentEditorScreen
import com.bagadbille.tdc.ui.mentor.assignments.AssignmentSubmissionsScreen
import com.bagadbille.tdc.ui.mentor.quiz.CreateQuizScreen
import com.bagadbille.tdc.ui.mentor.quiz.QuizResultsScreen
import com.bagadbille.tdc.ui.notifications.NotificationsScreen

@Composable
fun TdcNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToSetup = { navController.navigate(Screen.Setup.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToMain = { navController.navigate(Screen.Main.route) { popUpTo(Screen.Splash.route) { inclusive = true } } }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToMain = { isProfileComplete ->
                    if (isProfileComplete) {
                        navController.navigate(Screen.Main.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    } else {
                        navController.navigate(Screen.Setup.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    }
                }
            )
        }
        composable(Screen.Setup.route) {
            SetupScreen(
                onSetupComplete = { navController.navigate(Screen.Main.route) { popUpTo(Screen.Setup.route) { inclusive = true } } }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToQuizTaking = { navController.navigate("quizTaking/$it") },
                onNavigateToQuizResult = { navController.navigate("quizResult/$it") },
                onNavigateToAssignmentDetail = { navController.navigate(Screen.AssignmentDetail.createRoute(it)) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToCreateAssignment = { navController.navigate(Screen.AssignmentEditor.route) },
                onNavigateToAssignmentSubmissions = { navController.navigate(Screen.AssignmentSubmissions.createRoute(it)) },
                onNavigateToCreateQuiz = { navController.navigate(Screen.CreateQuiz.route) },
                onNavigateToQuizResults = { navController.navigate(Screen.QuizResults.createRoute(it)) },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Main.route) { inclusive = true } } }
            )
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
        composable(Screen.AssignmentEditor.route) {
            AssignmentEditorScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            Screen.AssignmentSubmissions.route,
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
        ) {
            AssignmentSubmissionsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.CreateQuiz.route) {
            CreateQuizScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            Screen.QuizResults.route,
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) {
            QuizResultsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
