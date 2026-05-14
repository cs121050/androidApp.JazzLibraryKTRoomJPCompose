package com.example.jazzlibraryktroomjpcompose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.jazzlibraryktroomjpcompose.ui.main.MainScreen
import com.example.jazzlibraryktroomjpcompose.ui.theme.JazzLibraryKTRoomJPComposeTheme
import com.example.jazzlibraryktroomjpcompose.ui.update.BlockingUpdateScreen
import com.example.jazzlibraryktroomjpcompose.ui.update.ForceUpdateManager
import com.example.jazzlibraryktroomjpcompose.ui.update.UpdateManager
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var updateManager: UpdateManager
    private var forceUpdateTriggered = false
    private var showBlockingUpdateScreen by mutableStateOf(false)
    private var pendingUpdateUrl by mutableStateOf("")
    private var forceUpdateManager: ForceUpdateManager? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starting, savedInstanceState=$savedInstanceState")
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: super.onCreate finished")
        //enableEdgeToEdge()
        Log.d(TAG, "onCreate: initializing UpdateManager")
        updateManager = UpdateManager(this)
        Log.d(TAG, "onCreate: UpdateManager initialized")

        Log.d(TAG, "onCreate: calling checkForForceUpdate()")
        // Check for force update immediately
        checkForForceUpdate()

        Log.d(TAG, "onCreate: setting Compose content")
        setContent {
            Log.d(TAG, "setContent: JazzLibraryKTRoomJPComposeTheme composition")
            JazzLibraryKTRoomJPComposeTheme {
                Log.d(TAG, "setContent: inside JazzLibraryKTRoomJPComposeTheme")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Log.d(TAG, "setContent: Box composition, showBlockingUpdateScreen=$showBlockingUpdateScreen")
                    if (showBlockingUpdateScreen) {
                        Log.d(TAG, "setContent: showing BlockingUpdateScreen with url=$pendingUpdateUrl")
                        BlockingUpdateScreen(
                            updateUrl = pendingUpdateUrl,
                            onUpdateStarted = {
                                Log.d(TAG, "BlockingUpdateScreen onUpdateStarted: invoked, finishing affinity after delay")
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    Log.d(TAG, "BlockingUpdateScreen onUpdateStarted: delayed finishAffinity called")
                                    finishAffinity()
                                }, 300)
                            }
                        )
                    } else {
                        Log.d(TAG, "setContent: showing MainScreen")
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
        Log.d(TAG, "onCreate: finished")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkForForceUpdate() {
        Log.d(TAG, "checkForForceUpdate: entered, forceUpdateTriggered=$forceUpdateTriggered")
        lifecycleScope.launch {
            Log.d(TAG, "checkForForceUpdate: coroutine started")
            try {
                Log.d(TAG, "checkForForceUpdate: fetching update info from UpdateManager")
                val updateInfo = updateManager.fetchUpdateInfo()
                Log.d(TAG, "checkForForceUpdate: fetchUpdateInfo result = $updateInfo")

                val currentCode = updateManager.getCurrentVersionCode()
                Log.d(TAG, "checkForForceUpdate: getCurrentVersionCode = $currentCode")

                val forceMinCode = updateInfo.forceMinVersionCode
                Log.d(TAG, "checkForForceUpdate: forceMinVersionCode = $forceMinCode")

                Log.d(TAG, "ForceUpdate")

                if (currentCode < forceMinCode && !forceUpdateTriggered) {
                    Log.d(TAG, "checkForForceUpdate: condition met (currentCode < forceMinCode AND not triggered)")
                    forceUpdateTriggered = true
                    Log.d(TAG, "checkForForceUpdate: forceUpdateTriggered set to true")
                    Log.d(TAG, "ForceUpdate")
                    showBlockingUpdateScreen = true
                    Log.d(TAG, "checkForForceUpdate: showBlockingUpdateScreen set to true")
                    pendingUpdateUrl = updateInfo.downloadUrl
                    Log.d(TAG, "checkForForceUpdate: pendingUpdateUrl = $pendingUpdateUrl")
                    forceUpdateManager = ForceUpdateManager(this@MainActivity)
                    Log.d(TAG, "checkForForceUpdate: ForceUpdateManager created, calling startForceUpdate")
                    forceUpdateManager!!.startForceUpdate(updateInfo.downloadUrl)
                    Log.d(TAG, "checkForForceUpdate: startForceUpdate finished")
                } else {
                    Log.d(TAG, "checkForForceUpdate: condition NOT met - currentCode=$currentCode, forceMinCode=$forceMinCode, forceUpdateTriggered=$forceUpdateTriggered")
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkForForceUpdate: exception caught", e)
                Log.e(TAG, "checkForForceUpdate: exception message = ${e.message}", e)
            }
            Log.d(TAG, "checkForForceUpdate: coroutine finished")
        }
        Log.d(TAG, "checkForForceUpdate: launched coroutine, exiting method")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: entered")
        super.onDestroy()
        Log.d(TAG, "onDestroy: super.onDestroy finished")
        Log.d(TAG, "onDestroy: calling forceUpdateManager?.cleanup()")
        forceUpdateManager?.cleanup()
        Log.d(TAG, "onDestroy: cleanup finished")
        Log.d(TAG, "onDestroy: exiting")
    }
}