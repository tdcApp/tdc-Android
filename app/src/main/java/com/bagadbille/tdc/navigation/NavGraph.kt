package com.bagadbille.tdc.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.bagadbille.tdc.ui.onboarding.OnboardingScreen
import com.bagadbille.tdc.ui.home.quiz.QuizResultScreen
import com.bagadbille.tdc.ui.home.quiz.QuizTakingScreen
import com.bagadbille.tdc.ui.main.MainScreen
import com.bagadbille.tdc.ui.mentor.assignments.AssignmentEditorScreen
import com.bagadbille.tdc.ui.mentor.assignments.AssignmentSubmissionsScreen
import com.bagadbille.tdc.ui.mentor.quiz.CreateQuizScreen
import com.bagadbille.tdc.ui.mentor.quiz.QuizResultsScreen
import com.bagadbille.tdc.ui.notifications.NotificationsScreen

@Composable
fun TdcNavGraph(
    navController: NavHostController = rememberNavController(),
    pendingNotification: Pair<String, String>? = null,
    onNotificationHandled: () -> Unit = {}
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToSetup = { navController.navigate(Screen.Setup.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToMain = { navController.navigate(Screen.Main.route) { popUpTo(Screen.Splash.route) { inclusive = true } } }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } } }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToMain = { isProfileComplete, userRole ->
                    if (com.bagadbille.tdc.data.model.isMentorRole(userRole) || isProfileComplete) {
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
        composable(Screen.Main.route) { backStackEntry ->
            var directNavTarget by remember { mutableStateOf<String?>(null) }

            val poppedTarget by backStackEntry.savedStateHandle
                .getStateFlow<String?>("NAV_SECTION", null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(poppedTarget) {
                poppedTarget?.let {
                    directNavTarget = it
                    backStackEntry.savedStateHandle.remove<String>("NAV_SECTION")
                }
            }

            LaunchedEffect(pendingNotification) {
                pendingNotification?.let { (type, _) ->
                    when (type.uppercase()) {
                        "QUIZ" -> {
                            directNavTarget = "QUIZ"
                        }
                        "ASSIGNMENT" -> {
                            directNavTarget = "ASSIGNMENT"
                        }
                        "ANNOUNCEMENT" -> {
                            navController.navigate(Screen.Notifications.route)
                        }
                        else -> {
                            navController.navigate(Screen.Notifications.route)
                        }
                    }
                    onNotificationHandled()
                }
            }

            MainScreen(
                onNavigateToQuizTaking = { navController.navigate("quizTaking/$it") },
                onNavigateToQuizResult = { navController.navigate("quizResult/$it") },
                onNavigateToAssignmentDetail = { navController.navigate(Screen.AssignmentDetail.createRoute(it)) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToCreateAssignment = { navController.navigate(Screen.AssignmentEditor.route) },
                onNavigateToAssignmentSubmissions = { navController.navigate(Screen.AssignmentSubmissions.createRoute(it)) },
                onNavigateToCreateQuiz = { navController.navigate(Screen.CreateQuiz.route) },
                onNavigateToQuizResults = { navController.navigate(Screen.QuizResults.createRoute(it)) },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Main.route) { inclusive = true } } },
                targetSection = directNavTarget,
                onTargetSectionConsumed = { directNavTarget = null }
            )
        }
        composable("quizTaking/{quizId}", arguments = listOf(navArgument("quizId") { type = NavType.StringType })) {
            val qId = it.arguments?.getString("quizId") ?: ""
            QuizTakingScreen(
                quizId = qId,
                onNavigateBack = { navController.popBackStack() },
                onQuizSubmitted = { isDisqualified ->
                    navController.navigate("quizResult/$qId?disqualified=$isDisqualified") {
                        popUpTo("quizTaking/$qId") { inclusive = true }
                    }
                }
            )
        }
        composable(
            "quizResult/{quizId}?disqualified={disqualified}",
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType },
                navArgument("disqualified") { type = NavType.BoolType; defaultValue = false }
            )
        ) {
            val qId = it.arguments?.getString("quizId") ?: ""
            val isDisqualified = it.arguments?.getBoolean("disqualified") ?: false
            QuizResultScreen(
                quizId = qId,
                isDisqualified = isDisqualified,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuiz = { _ ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("NAV_SECTION", "QUIZ")
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                },
                onNavigateToAssignment = { _ ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("NAV_SECTION", "ASSIGNMENT")
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                },
                onNavigateToSchedule = {
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                }
            )
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
