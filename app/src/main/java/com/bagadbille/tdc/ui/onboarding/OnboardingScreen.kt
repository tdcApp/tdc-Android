package com.bagadbille.tdc.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bagadbille.tdc.ui.auth.AuthViewModel
import com.bagadbille.tdc.ui.components.AmbientGlowBackground
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val handleFinish = {
        viewModel.completeOnboarding()
        onNavigateToLogin()
    }

    AmbientGlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 44.dp, bottom = 36.dp)
        ) {
            // Top Bar: Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { handleFinish() }
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Carousel Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Illustration
                    when (page) {
                        0 -> LearnTogetherIllustration()
                        1 -> ConnectWithDevelopersIllustration()
                        2 -> GrowYourCareerIllustration()
                    }

                    Spacer(Modifier.height(36.dp))

                    // Title
                    val titleAnnotated = when (page) {
                        0 -> buildAnnotatedString {
                            append("Learn ")
                            withStyle(SpanStyle(color = Color(0xFFFF8A32), fontWeight = FontWeight.ExtraBold)) {
                                append("Together")
                            }
                        }
                        1 -> buildAnnotatedString {
                            append("Connect with\n")
                            withStyle(SpanStyle(color = Color(0xFFFF8A32), fontWeight = FontWeight.ExtraBold)) {
                                append("Developers")
                            }
                        }
                        else -> buildAnnotatedString {
                            append("Grow Your\n")
                            withStyle(SpanStyle(color = Color(0xFFFF8A32), fontWeight = FontWeight.ExtraBold)) {
                                append("Career")
                            }
                        }
                    }

                    Text(
                        text = titleAnnotated,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(14.dp))

                    // Description
                    val description = when (page) {
                        0 -> "Access tutorials, resources and hands-on projects to grow your skills."
                        1 -> "Join a supportive community, ask questions, share ideas and collaborate."
                        else -> "Participate in events, hackathons and real-world projects to unlock new opportunities."
                    }

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Bottom Navigation Row: Indicator Dots + Circular Next Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val widthAnim = animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(widthAnim.value)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) Color(0xFF55D6E8)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                // Circular Cyan Arrow Next / Finish Button
                IconButton(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            handleFinish()
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF55D6E8),
                        contentColor = Color(0xFF0B0B0D)
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color(0xFF0B0B0D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
