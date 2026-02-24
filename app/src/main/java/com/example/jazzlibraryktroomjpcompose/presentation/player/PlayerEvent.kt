// PlayerEvent.kt
package com.example.jazzlibraryktroomjpcompose.presentation.player

import com.example.jazzlibraryktroomjpcompose.domain.models.FilterPath

/**
 * Events emitted by PlayerViewModel that the UI (MainScreen) should handle.
 */
sealed class PlayerEvent {
    // Request to scroll the list to the active card (by its ID)
    data class ScrollToCard(val cardId: String) : PlayerEvent()

    // Request to restore a previous filter path because the active card is missing
    data class RestoreFilterPath(val filterPath: List<FilterPath>) : PlayerEvent()

    // Optional: request to go fullscreen
    object RequestFullScreen : PlayerEvent()
}