package com.bagadbille.tdc.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.bagadbille.tdc.ui.home.classes.ClassesScreen
import com.bagadbille.tdc.ui.home.general.GeneralScreen
import com.bagadbille.tdc.ui.home.quiz.QuizListScreen
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToClassDetail: (String) -> Unit,
    onNavigateToQuizTaking: (String) -> Unit,
    onNavigateToQuizResult: (String) -> Unit
) {
    val tabs = listOf("General", "Classes", "Quiz")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage, containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = pagerState.currentPage == index, onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = MaterialTheme.colorScheme.primary, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> GeneralScreen()
                1 -> ClassesScreen(onNavigateToClassDetail)
                2 -> QuizListScreen(onNavigateToQuizTaking, onNavigateToQuizResult)
            }
        }
    }
}
