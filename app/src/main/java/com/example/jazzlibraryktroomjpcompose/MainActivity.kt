package com.example.jazzlibraryktroomjpcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.jazzlibraryktroomjpcompose.ui.main.MainScreen
import com.example.jazzlibraryktroomjpcompose.ui.theme.JazzLibraryKTRoomJPComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.jazzlibraryktroomjpcompose.ui.update.UpdateManager
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var updateManager: UpdateManager
    private var forceUpdateTriggered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateManager = UpdateManager(this)
        // Check for force update immediately
        checkForForceUpdate()
        setContent {
            JazzLibraryKTRoomJPComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun checkForForceUpdate() {
        lifecycleScope.launch {
            try {
                val updateInfo = updateManager.fetchUpdateInfo()
                val currentVersion = updateManager.getCurrentVersion()
                val forceMinVersion = updateInfo.forceMinVersion

                Log.d("MainActivity", "Current: $currentVersion, Force min: $forceMinVersion")

                // If current version is older than the forced minimum version, trigger force update
                if (updateManager.compareVersions(currentVersion, forceMinVersion) < 0 && !forceUpdateTriggered) {
                    forceUpdateTriggered = true
                    Log.d("MainActivity", "🚨 Force update triggered! Opening download URL...")
                    updateManager.openDownloadUrl()
                    // Optional: finish() to close the app? Usually leave it open, user can install then come back.
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Force update check failed", e)
            }
        }
    }
}

