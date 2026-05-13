package com.example.jazzlibraryktroomjpcompose.domain.models

/**
 * Data model for app version information retrieved from Firebase Remote Config.
 */
data class UpdateInfo(
    val currentVersion: String = "1.0",
    val latestVersion: String = "1.0",
    val updateType: UpdateType = UpdateType.NONE, // NONE, SOFT, FORCE
    val updateUrl: String = "",
    val changeLog: String = "No updates available",
    val lastUpdateDate: Long = System.currentTimeMillis(),
    val forceMinVersion: String? = null // If current version is below this, force update
)

enum class UpdateType {
    NONE,      // No update needed
    SOFT,      // Optional update (show banner)
    FORCE      // Mandatory update (auto-update without permission)
}

/**
 * Update state for UI
 */
sealed class UpdateState {
    object NoUpdate : UpdateState()
    data class SoftUpdateAvailable(val info: UpdateInfo) : UpdateState()
    data class ForceUpdateAvailable(val info: UpdateInfo) : UpdateState()
    object Updating : UpdateState()
    object UpdateComplete : UpdateState()
}
