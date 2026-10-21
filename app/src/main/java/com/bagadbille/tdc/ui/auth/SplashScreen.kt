package com.bagadbille.tdc.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bagadbille.tdc.R
import com.bagadbille.tdc.ui.components.AmbientGlowBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.isLoggedIn.collectAsState(initial = null)
    val profileComplete by viewModel.isProfileComplete.collectAsState(initial = null)
    val userRole by viewModel.userRole.collectAsState(initial = null)
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState(initial = null)
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(900))
    }

    LaunchedEffect(authState, profileComplete, userRole, hasCompletedOnboarding) {
        if (hasCompletedOnboarding != null && authState != null) {
            delay(1200)
            when {
                !hasCompletedOnboarding!! -> onNavigateToOnboarding()
                !authState!! -> onNavigateToLogin()
                com.bagadbille.tdc.data.model.isMentorRole(userRole) || profileComplete == true -> onNavigateToMain()
                else -> onNavigateToSetup()
            }
        }
    }

    AmbientGlowBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(alpha.value)
            ) {
                Image(
                    painter = painterResource(R.drawable.tdc_logo),
                    contentDescription = "TIT Developers Logo",
                    modifier = Modifier.size(105.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "TIT DEVELOPERS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Build  ·  Learn  ·  Connect  ·  Grow",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(48.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

