package com.bagadbille.tdc.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bagadbille.tdc.ui.home.classes.ClassesScreen
import com.bagadbille.tdc.ui.home.classes.ClassesViewModel
import com.bagadbille.tdc.ui.home.general.GeneralScreen
import com.bagadbille.tdc.ui.home.general.GeneralViewModel
import com.bagadbille.tdc.ui.home.quiz.QuizListScreen
import com.bagadbille.tdc.ui.home.quiz.QuizViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToQuizTaking: (String) -> Unit,
    onNavigateToQuizResult: (String) -> Unit,
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false
) {
    val tabs = listOf("General", "Classes", "Quiz")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    val generalViewModel: GeneralViewModel = hiltViewModel()
    val classesViewModel: ClassesViewModel = hiltViewModel()
    val quizViewModel: QuizViewModel = hiltViewModel()

    Column(Modifier.fillMaxSize()) {
        com.bagadbille.tdc.ui.components.TdcHeader(
            onNotificationClick = onNotificationClick,
            hasUnreadNotifications = hasUnreadNotifications,
            onRefreshClick = {
                when (pagerState.currentPage) {
                    0 -> generalViewModel.loadDashboard()
                    1 -> classesViewModel.loadClasses()
                    2 -> quizViewModel.loadQuizzes(isPullToRefresh = true)
                }
            }
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = pagerState.currentPage == index, onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = MaterialTheme.colorScheme.primary, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> GeneralScreen(
                    viewModel = generalViewModel,
                    onQuizClick = onNavigateToQuizTaking,
                    onNavigateToClasses = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onNavigateToQuizzes = { scope.launch { pagerState.animateScrollToPage(2) } }
                )
                1 -> ClassesScreen(viewModel = classesViewModel)
                2 -> QuizListScreen(
                    onNavigateToQuizTaking = onNavigateToQuizTaking,
                    onNavigateToQuizResult = onNavigateToQuizResult,
                    viewModel = quizViewModel
                )
            }
        }
    }
}

