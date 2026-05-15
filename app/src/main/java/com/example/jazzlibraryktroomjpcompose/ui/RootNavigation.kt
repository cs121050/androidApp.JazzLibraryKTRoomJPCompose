// app/src/main/java/com/example/jazzlibraryktroomjpcompose/ui/RootNavigation.kt

package com.example.jazzlibraryktroomjpcompose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jazzlibraryktroomjpcompose.ui.main.MainScreen
import com.example.jazzlibraryktroomjpcompose.ui.tos.TosScreen
import com.example.jazzlibraryktroomjpcompose.ui.tos.TosViewModel

@Composable
fun RootNavigation() {
    val tosViewModel: TosViewModel = hiltViewModel()
    val isTosAccepted by tosViewModel.isTosAccepted.collectAsState()

    if (isTosAccepted) {
        // User accepted ToS, show main app
        MainScreen()
    } else {
        // User hasn't accepted ToS, show ToS screen
        TosScreen(
            viewModel = tosViewModel,
            onAccept = {
                // ToS acceptance handled in TosScreen, this triggers recomposition
            }
        )
    }
}