package com.bagadbille.tdc.ui.mentor.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.bagadbille.tdc.ui.components.TdcHeader
import com.bagadbille.tdc.ui.mentor.quiz.QuizTab
import kotlinx.coroutines.launch

/** Mentor's middle tab — Overview · Batches · Quiz */
@Composable
fun MentorDashboardScreen(
    onNotificationClick: () -> Unit = {},
    onCreateAssignment: () -> Unit = {},
    onCreateQuiz: () -> Unit = {},
    onOpenQuizResults: (String) -> Unit = {},
    onNavigateToAssignmentsTab: () -> Unit = {},
    hasUnreadNotifications: Boolean = false
) {
    val tabs = listOf("Overview", "Classes", "Quiz")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    val overviewViewModel: OverviewViewModel = hiltViewModel()
    val rosterViewModel: RosterViewModel = hiltViewModel()
    val quizViewModel: com.bagadbille.tdc.ui.mentor.quiz.MentorQuizViewModel = hiltViewModel()

    Column(Modifier.fillMaxSize()) {
        TdcHeader(
            onNotificationClick = onNotificationClick,
            hasUnreadNotifications = hasUnreadNotifications,
            onRefreshClick = {
                when (pagerState.currentPage) {
                    0 -> overviewViewModel.load()
                    1 -> rosterViewModel.load()
                    2 -> quizViewModel.load(isPullToRefresh = true)
                }
            }
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> OverviewTab(
                    viewModel = overviewViewModel,
                    onNavigateToAssignments = onNavigateToAssignmentsTab,
                    onNavigateToQuiz = { scope.launch { pagerState.animateScrollToPage(2) } }
                )
                1 -> {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        if (rosterViewModel.uiState.value is RosterUiState.Loading) {
                            rosterViewModel.load()
                        }
                    }
                    RosterTab(viewModel = rosterViewModel)
                }
                else -> {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        if (quizViewModel.uiState.value is com.bagadbille.tdc.ui.mentor.quiz.MentorQuizUiState.Loading) {
                            quizViewModel.load()
                        }
                    }
                    QuizTab(
                        viewModel = quizViewModel,
                        onCreateQuiz = onCreateQuiz,
                        onOpenQuizResults = onOpenQuizResults
                    )
                }
            }
        }
    }
}
