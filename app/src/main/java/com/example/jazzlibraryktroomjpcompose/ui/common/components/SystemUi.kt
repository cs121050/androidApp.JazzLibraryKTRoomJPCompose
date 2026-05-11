package com.example.jazzlibraryktroomjpcompose.ui.common.components

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@Composable
fun SetStatusBarColor(color: Color) {
    val context = LocalContext.current
    val window = (context as? ComponentActivity)?.window

    DisposableEffect(window, color) {
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // setStatusBarColor is NOT deprecated – it's a stable API since Lollipop
            window.setStatusBarColor(color.toArgb())
        }
        onDispose { } // Required by DisposableEffect, but we don't need to restore
    }
}

@Composable
fun SetNavigationBarColor(color: Color) {
    val context = LocalContext.current
    val window = (context as? ComponentActivity)?.window

    DisposableEffect(window, color) {
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color.toArgb())
        }
        onDispose { }
    }
}