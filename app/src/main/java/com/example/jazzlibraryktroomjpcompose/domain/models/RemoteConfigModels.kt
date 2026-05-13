package com.example.jazzlibraryktroomjpcompose.domain.models

/**
 * ✅ DOMAIN LAYER - Remote Config Models
 * NO Firebase dependency
 */
data class AppUpdateInfo(
    val latestVersionCode: Int = 1,
    val currentVersionCode: Int = 1,
    val updateUrl: String = "",
    val updateTitle: String = "New Update Available",
    val updateMessage: String = "A new version of the app is available.",
    val isMandatory: Boolean = false,  // true = force update, false = optional
    val releaseNotes: String = ""
)

data class FeatureFlags(
    val enableNewFilter: Boolean = false,
    val enableAnalytics: Boolean = true,
    val enableRatings: Boolean = true,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "App is under maintenance. Please try again later."
)

data class ContentConfig(
    val apiBaseUrl: String = "https://api.example.com",
    val cacheTimeout: Long = 3600000,  // 1 hour in ms
    val maxSearchResults: Int = 100,
    val enableOfflineMode: Boolean = true
)

sealed class RemoteConfigState {
    object Loading : RemoteConfigState()
    data class Success(
        val updateInfo: AppUpdateInfo,
        val features: FeatureFlags,
        val config: ContentConfig
    ) : RemoteConfigState()
    data class Error(val exception: Exception) : RemoteConfigState()
}