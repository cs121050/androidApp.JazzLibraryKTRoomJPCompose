package com.example.jazzlibraryktroomjpcompose.ui.main


import androidx.compose.runtime.*

enum class CustomSheetValue {
    Hidden,
    HalfExpanded,
    Expanded
}

class CustomSheetState(
    initialValue: CustomSheetValue = CustomSheetValue.Hidden
) {
    var currentValue by mutableStateOf(initialValue)
        private set

    fun expand() {
        currentValue = CustomSheetValue.Expanded
    }

    fun halfExpand() {
        currentValue = CustomSheetValue.HalfExpanded
    }

    fun hide() {
        currentValue = CustomSheetValue.Hidden
    }
}

@Composable
fun rememberCustomSheetState(
    initialValue: CustomSheetValue = CustomSheetValue.Hidden
): CustomSheetState {
    return remember { CustomSheetState(initialValue = initialValue) }
}