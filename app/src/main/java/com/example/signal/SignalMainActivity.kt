package com.example.signal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NetworkMonitor

class SignalMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.data.SnxCloudSyncService.startAutoSync(this)
        val networkMonitor = NetworkMonitor(applicationContext)
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val vm = remember { SignalViewModel(networkMonitor) }
                SignalScreen(viewModel = vm)
            }
        }
    }
}
