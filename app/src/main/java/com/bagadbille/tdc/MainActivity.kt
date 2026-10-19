package com.bagadbille.tdc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.repository.NotificationRepository
import com.bagadbille.tdc.data.service.TdcFirebaseMessagingService
import com.bagadbille.tdc.navigation.TdcNavGraph
import com.bagadbille.tdc.ui.theme.TDCTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private var pendingNotificationDestination by mutableStateOf<Pair<String, String>?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.w("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()
        syncFcmToken()
        handleIntent(intent)

        setContent {
            val isDarkTheme by dataStoreManager.isDarkTheme.collectAsStateWithLifecycle(initialValue = true)
            TDCTheme(darkTheme = isDarkTheme) {
                TdcNavGraph(
                    pendingNotification = pendingNotificationDestination,
                    onNotificationHandled = { pendingNotificationDestination = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val type = intent?.getStringExtra(TdcFirebaseMessagingService.EXTRA_TYPE)
        val id = intent?.getStringExtra(TdcFirebaseMessagingService.EXTRA_ID)
        if (!type.isNullOrBlank()) {
            pendingNotificationDestination = Pair(type, id ?: "")
        }
    }

    override fun onResume() {
        super.onResume()
        syncFcmToken()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun syncFcmToken() {
        lifecycleScope.launch {
            val token = dataStoreManager.authToken.firstOrNull()
            if (!token.isNullOrBlank()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrBlank()) {
                        val fcmToken = task.result
                        lifecycleScope.launch {
                            notificationRepository.registerFcmToken(fcmToken).fold(
                                onSuccess = {
                                    Log.i("TDC_FCM", "Successfully registered FCM token with backend: $fcmToken")
                                },
                                onFailure = { e ->
                                    Log.e("TDC_FCM", "Failed to register FCM token with backend", e)
                                }
                            )
                        }
                    } else {
                        Log.w("TDC_FCM", "Failed to retrieve FCM token from Firebase", task.exception)
                    }
                }
            } else {
                Log.d("TDC_FCM", "No authToken found in DataStore; skipping FCM sync")
            }
        }
    }
}