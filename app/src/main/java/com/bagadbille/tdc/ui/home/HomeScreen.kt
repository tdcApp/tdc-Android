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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bagadbille.tdc.ui.home.classes.ClassesScreen
import com.bagadbille.tdc.ui.home.general.GeneralScreen
import com.bagadbille.tdc.ui.home.quiz.QuizListScreen
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToQuizTaking: (String) -> Unit,
    onNavigateToQuizResult: (String) -> Unit,
    onNotificationClick: () -> Unit = {}
) {
    val tabs = listOf("General", "Classes", "Quiz")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        // Header: Logo | TDC | Notification Bell
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.School, "Logo", Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                "TDC", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onNotificationClick) {
                BadgedBox(badge = { Badge(containerColor = MaterialTheme.colorScheme.error, modifier = Modifier.size(8.dp)) }) {
                    Icon(Icons.Outlined.Notifications, "Notifications", Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

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
                    onQuizClick = onNavigateToQuizTaking,
                    onNavigateToClasses = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onNavigateToQuizzes = { scope.launch { pagerState.animateScrollToPage(2) } }
                )
                1 -> ClassesScreen()
                2 -> QuizListScreen(onNavigateToQuizTaking, onNavigateToQuizResult)
            }
        }
    }
}
