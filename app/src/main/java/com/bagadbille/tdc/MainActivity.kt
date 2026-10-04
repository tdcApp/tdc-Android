package com.bagadbille.tdc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.navigation.TdcNavGraph
import com.bagadbille.tdc.ui.theme.TDCTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by dataStoreManager.isDarkTheme.collectAsStateWithLifecycle(initialValue = true)
            TDCTheme(darkTheme = isDarkTheme) {
                TdcNavGraph()
            }
        }
    }
}