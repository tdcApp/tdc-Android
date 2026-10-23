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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bagadbille.tdc.ui.home.classes.ClassesScreen
import com.bagadbille.tdc.ui.home.classes.ClassesViewModel
import com.bagadbille.tdc.ui.home.general.GeneralScreen
import com.bagadbille.tdc.ui.home.general.GeneralViewModel
import androidx.activity.compose.BackHandler
import com.bagadbille.tdc.ui.home.quiz.QuizListScreen
import com.bagadbille.tdc.ui.home.quiz.QuizViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToQuizTaking: (String) -> Unit,
    onNavigateToQuizResult: (String) -> Unit,
    onNavigateToAssignments: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    targetPage: Int? = null,
    onTargetPageConsumed: () -> Unit = {},
    refreshQuizSignal: Long = 0L
) {
    val tabs = listOf("Overview", "Classes", "Quiz")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    val generalViewModel: GeneralViewModel = hiltViewModel()
    val classesViewModel: ClassesViewModel = hiltViewModel()
    val quizViewModel: QuizViewModel = hiltViewModel()

    LaunchedEffect(targetPage) {
        targetPage?.let { page ->
            pagerState.scrollToPage(page)
            onTargetPageConsumed()
        }
    }

    LaunchedEffect(refreshQuizSignal) {
        if (refreshQuizSignal > 0L) {
            quizViewModel.loadQuizzes(isPullToRefresh = true)
        }
    }

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
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.then(with(TabRowDefaults) { Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]) }),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> GeneralScreen(
                    viewModel = generalViewModel,
                    onQuizClick = onNavigateToQuizTaking,
                    onQuizResultClick = onNavigateToQuizResult,
                    onNavigateToClasses = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onNavigateToQuizzes = { scope.launch { pagerState.animateScrollToPage(2) } },
                    onNavigateToAssignments = onNavigateToAssignments,
                    onNavigateToNotices = onNotificationClick
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

