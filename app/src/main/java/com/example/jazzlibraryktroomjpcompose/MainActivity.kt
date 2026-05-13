package com.example.jazzlibraryktroomjpcompose

import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.jazzlibraryktroomjpcompose.ui.update.BlockingUpdateScreen
import com.example.jazzlibraryktroomjpcompose.ui.update.ForceUpdateManager
import com.example.jazzlibraryktroomjpcompose.ui.update.UpdateManager
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var updateManager: UpdateManager
    private var forceUpdateTriggered = false
    private var showBlockingUpdateScreen by mutableStateOf(false)
    private var pendingUpdateUrl by mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        BlockingUpdateScreen(
                            updateUrl = pendingUpdateUrl,
                            onUpdateStarted = {
                                // After the screen shows the message, we close the app.
                                // The browser will already be opened by openDownloadUrl() inside the screen.
                                // We delay finishing to ensure the intent is sent.
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    finishAffinity()
                                }, 300)
                            }
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainScreen()
                        }
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkForForceUpdate() {
        lifecycleScope.launch {
            try {
                val updateInfo = updateManager.fetchUpdateInfo()
                val currentCode = updateManager.getCurrentVersionCode()
                val forceMinCode = updateInfo.forceMinVersionCode

                if (currentCode < forceMinCode && !forceUpdateTriggered) {
                    forceUpdateTriggered = true
                    Log.d("ForceUpdate", "🚨 Starting automatic force update download")
                    // Start silent download + installation
                    ForceUpdateManager(this@MainActivity).startForceUpdate(updateInfo.downloadUrl)
                }
            } catch (e: Exception) {
                Log.e("ForceUpdate", "Error during check", e)
            }
        }
    }
}

