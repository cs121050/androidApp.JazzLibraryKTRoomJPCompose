// app/src/main/java/com/example/jazzlibraryktroomjpcompose/MainActivity.kt

package com.example.jazzlibraryktroomjpcompose

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.jazzlibraryktroomjpcompose.ui.RootNavigation  // ← ADD THIS IMPORT
import com.example.jazzlibraryktroomjpcompose.ui.theme.JazzLibraryKTRoomJPComposeTheme
import com.example.jazzlibraryktroomjpcompose.ui.update.BlockingUpdateScreen
import com.example.jazzlibraryktroomjpcompose.ui.update.ForceUpdateService
import com.example.jazzlibraryktroomjpcompose.ui.update.UpdateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var updateManager: UpdateManager
    private var forceUpdateTriggered = false
    private var showBlockingUpdateScreen by mutableStateOf(false)
    private var pendingUpdateUrl by mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: called")

        //enableEdgeToEdge()
        updateManager = UpdateManager(this)

        // Check for force update immediately
        checkForForceUpdate()

        setContent {
            JazzLibraryKTRoomJPComposeTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (showBlockingUpdateScreen) {
                        Log.d("MainActivity", "setContent: showing BlockingUpdateScreen")
                        BlockingUpdateScreen(
                            updateUrl = pendingUpdateUrl,
                            onUpdateStarted = {
                                Log.d("MainActivity", "BlockingUpdateScreen onUpdateStarted: update started")
                            }
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            RootNavigation()  // ← CHANGED FROM MainScreen()
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkForForceUpdate() {
        Log.d("MainActivity", "checkForForceUpdate: starting check")

        lifecycleScope.launch {
            try {
                val updateInfo = updateManager.fetchUpdateInfo()
                val currentCode = updateManager.getCurrentVersionCode()
                val forceMinCode = updateInfo.forceMinVersionCode

                Log.d("MainActivity", "checkForForceUpdate: current=$currentCode, forceMin=$forceMinCode")

                if (currentCode < forceMinCode && !forceUpdateTriggered) {
                    forceUpdateTriggered = true
                    Log.d("MainActivity", "🚨 Force update detected! Starting ForceUpdateService")

                    showBlockingUpdateScreen = true
                    pendingUpdateUrl = updateInfo.downloadUrl

                    val serviceIntent = Intent(this@MainActivity, ForceUpdateService::class.java).apply {
                        putExtra("APK_URL", updateInfo.downloadUrl)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    Log.d("MainActivity", "checkForForceUpdate: ForceUpdateService started")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "checkForForceUpdate: error", e)
            }
        }
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy: called")
        super.onDestroy()
    }
}