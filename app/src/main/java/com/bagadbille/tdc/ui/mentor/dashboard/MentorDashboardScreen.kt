package com.bagadbille.tdc.ui.mentor.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.bagadbille.tdc.ui.components.TdcHeader
import com.bagadbille.tdc.ui.mentor.quiz.QuizTab
import kotlinx.coroutines.launch

/** Mentor's middle tab — Overview · Batches · Quiz */
@Composable
fun MentorDashboardScreen(
    onNotificationClick: () -> Unit = {},
    onCreateAssignment: () -> Unit = {},
    onCreateQuiz: () -> Unit = {},
    onOpenQuizResults: (String) -> Unit = {}
) {
    val tabs = listOf("Overview", "Batches", "Quiz")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        TdcHeader(onNotificationClick = onNotificationClick)

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
                    onCreateAssignment = onCreateAssignment,
                    onCreateQuiz = onCreateQuiz
                )
                1 -> RosterTab()
                else -> QuizTab(
                    onCreateQuiz = onCreateQuiz,
                    onOpenQuizResults = onOpenQuizResults
                )
            }
        }
    }
}
