package com.bagadbille.tdc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bagadbille.tdc.navigation.TdcNavGraph
import com.bagadbille.tdc.ui.theme.TDCTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TDCTheme {
                TdcNavGraph()
            }
        }
    }
}