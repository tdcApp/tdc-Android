package com.bagadbille.tdc.ui.home.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.ui.components.CyberCard
import com.bagadbille.tdc.ui.components.EmptyStateScreen
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(viewModel: ClassesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = uiState) {
        is ClassesUiState.Loading -> LoadingScreen()
        is ClassesUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadClasses() })
        is ClassesUiState.Success -> {
            val data = s.data
            val isEnrolledTab = data.selectedFilter == ClassFilterTab.ENROLLED
            val displayedClasses = if (isEnrolledTab) data.enrolledClasses else data.allClasses

            com.bagadbille.tdc.ui.components.TdcPullToRefresh(
                isRefreshing = false,
                onRefresh = { viewModel.loadClasses() }
            ) {
                if (isEnrolledTab && data.enrolledClasses.isEmpty()) {
                    Column(Modifier.fillMaxSize()) {
                        FilterChipsRow(
                            selectedFilter = data.selectedFilter,
                            enrolledCount = data.enrolledClasses.size,
                            allCount = data.allClasses.size,
                            onFilterSelect = { viewModel.setFilterTab(it) }
                        )
                        EmptyStateScreen(
                            Icons.Outlined.School,
                            "No Classes Enrolled",
                            "You are not enrolled in any batches yet."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            FilterChipsRow(
                                selectedFilter = data.selectedFilter,
                                enrolledCount = data.enrolledClasses.size,
                                allCount = data.allClasses.size,
                                onFilterSelect = { viewModel.setFilterTab(it) }
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (isEnrolledTab) "Your Batches" else "All Batches",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${displayedClasses.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        items(displayedClasses, key = { it.id }) { classInfo ->
                            val isEnrolled = data.enrolledClassIds.contains(classInfo.id)
                            ClassCard(c = classInfo, isEnrolled = isEnrolled)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: ClassFilterTab,
    enrolledCount: Int,
    allCount: Int,
    onFilterSelect: (ClassFilterTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == ClassFilterTab.ENROLLED,
            onClick = { onFilterSelect(ClassFilterTab.ENROLLED) },
            label = {
                Text(
                    "Enrolled ($enrolledCount)",
                    fontWeight = if (selectedFilter == ClassFilterTab.ENROLLED) FontWeight.Bold else FontWeight.Normal
                )
            },
            leadingIcon = if (selectedFilter == ClassFilterTab.ENROLLED) {
                {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null
        )

        FilterChip(
            selected = selectedFilter == ClassFilterTab.ALL,
            onClick = { onFilterSelect(ClassFilterTab.ALL) },
            label = {
                Text(
                    "All Batches ($allCount)",
                    fontWeight = if (selectedFilter == ClassFilterTab.ALL) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}

@Composable
private fun ClassCard(c: ClassInfo, isEnrolled: Boolean) {
    val icon = when {
        c.type.contains("tech", ignoreCase = true) -> Icons.Outlined.Devices
        c.name.contains("python", ignoreCase = true) || c.name.contains("c++", ignoreCase = true) -> Icons.Outlined.Terminal
        else -> Icons.Outlined.Code
    }

    val accentColor = if (isEnrolled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = if (isEnrolled) accentColor.copy(alpha = 0.35f) else GlassBorderSubtle
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = accentColor
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    c.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            c.type.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))

                    if (isEnrolled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Enrolled",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Available",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

