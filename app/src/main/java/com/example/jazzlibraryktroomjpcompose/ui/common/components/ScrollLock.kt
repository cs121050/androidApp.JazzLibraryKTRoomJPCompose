package com.example.jazzlibraryktroomjpcompose.ui.common.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ScrollLockState { var isLocked by mutableStateOf(false) }
val LocalScrollLock = compositionLocalOf { ScrollLockState() }