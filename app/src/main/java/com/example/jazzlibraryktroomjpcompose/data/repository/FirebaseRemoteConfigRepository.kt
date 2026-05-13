package com.example.jazzlibraryktroomjpcompose.data.repository

import android.content.Context
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.example.jazzlibraryktroomjpcompose.domain.models.AppUpdateInfo
import com.example.jazzlibraryktroomjpcompose.domain.models.ContentConfig
import com.example.jazzlibraryktroomjpcompose.domain.models.FeatureFlags
import com.example.jazzlibraryktroomjpcompose.domain.repository.RemoteConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

/**
 * ✅ DATA LAYER - Firebase Remote Config Implementation
 * Fetches config from Firebase and returns domain models
 */
@Singleton
class FirebaseRemoteConfigRepository(
    private val remoteConfig: FirebaseRemoteConfig,
    private val context: Context
) : RemoteConfigRepository {

    companion object {
        private const val TAG = "RemoteConfigRepo"

        // Default cache duration (1 hour in production, 0 for testing)
        private const val CACHE_EXPIRATION_SECONDS = 3600L
        // Force updates
        private const val CACHE_EXPIRATION_SECONDS_FORCE_UPDATE = 0L


        // Remote Config keys
        private const val KEY_LATEST_VERSION = "latest_version_code"
        private const val KEY_UPDATE_URL = "update_url"
        private const val KEY_UPDATE_TITLE = "update_title"
        private const val KEY_UPDATE_MESSAGE = "update_message"
        private const val KEY_MANDATORY_UPDATE = "mandatory_update"
        private const val KEY_RELEASE_NOTES = "release_notes"

        private const val KEY_ENABLE_NEW_FILTER = "enable_new_filter"
        private const val KEY_ENABLE_ANALYTICS = "enable_analytics"
        private const val KEY_ENABLE_RATINGS = "enable_ratings"
        private const val KEY_MAINTENANCE_MODE = "maintenance_mode"
        private const val KEY_MAINTENANCE_MESSAGE = "maintenance_message"

        private const val KEY_API_BASE_URL = "api_base_url"
        private const val KEY_CACHE_TIMEOUT = "cache_timeout_ms"
        private const val KEY_MAX_SEARCH_RESULTS = "max_search_results"
        private const val KEY_ENABLE_OFFLINE = "enable_offline_mode"
    }

    init {
        // Configure Remote Config settings
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(CACHE_EXPIRATION_SECONDS_FORCE_UPDATE)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)

        // Set default values (fallback if Firebase fetch fails)
        remoteConfig.setDefaultsAsync(getDefaultValues())

        Log.d(TAG, "Remote Config initialized")
    }

    /**
     * Fetch latest config from Firebase and activate it
     */
    override suspend fun fetchAndActivate(): Result<Unit> {
        return try {
            Log.d(TAG, "Fetching remote config from Firebase...")

            // Fetch from Firebase
            remoteConfig.fetchAndActivate().await()

            Log.d(TAG, "Remote config fetched and activated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch remote config", e)
            // Don't fail - use cached values or defaults
            Result.failure(e)
        }
    }

    /**
     * Get app update information
     */
    override fun getAppUpdateInfo(): Flow<AppUpdateInfo> = flowOf(
        AppUpdateInfo(
            latestVersionCode = getInt(KEY_LATEST_VERSION, 1),
            currentVersionCode = getCurrentAppVersionCode(),
            updateUrl = getString(KEY_UPDATE_URL, "https://play.google.com/store/apps/details?id=com.example.jazzlibraryktroomjpcompose"),
            updateTitle = getString(KEY_UPDATE_TITLE, "New Update Available"),
            updateMessage = getString(KEY_UPDATE_MESSAGE, "A new version is available. Please update."),
            isMandatory = remoteConfig.getBoolean(KEY_MANDATORY_UPDATE),
            releaseNotes = getString(KEY_RELEASE_NOTES, "")
        )
    )

    /**
     * Get feature flags
     */
    override fun getFeatureFlags(): Flow<FeatureFlags> = flowOf(
        FeatureFlags(
            enableNewFilter = remoteConfig.getBoolean(KEY_ENABLE_NEW_FILTER),
            enableAnalytics = remoteConfig.getBoolean(KEY_ENABLE_ANALYTICS),
            enableRatings = remoteConfig.getBoolean(KEY_ENABLE_RATINGS),
            maintenanceMode = remoteConfig.getBoolean(KEY_MAINTENANCE_MODE),
            maintenanceMessage = getString(KEY_MAINTENANCE_MESSAGE, "App is under maintenance.")
        )
    )

    /**
     * Get content configuration
     */
    override fun getContentConfig(): Flow<ContentConfig> = flowOf(
        ContentConfig(
            apiBaseUrl = getString(KEY_API_BASE_URL, "https://api.example.com"),
            cacheTimeout = remoteConfig.getLong(KEY_CACHE_TIMEOUT),
            maxSearchResults = remoteConfig.getLong(KEY_MAX_SEARCH_RESULTS).toInt(),
            enableOfflineMode = remoteConfig.getBoolean(KEY_ENABLE_OFFLINE)
        )
    )

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return remoteConfig.getBoolean(key)
    }

    override fun getString(key: String, default: String): String {
        return remoteConfig.getString(key).takeIf { it.isNotEmpty() } ?: default
    }

    override fun getInt(key: String, default: Int): Int {
        return remoteConfig.getLong(key).toInt().takeIf { it != 0L.toInt() } ?: default
    }

    /**
     * Get current app version code
     */
    private fun getCurrentAppVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app version code", e)
            1  // Default fallback
        }
    }

    /**
     * Default Remote Config values (fallback)
     */
    private fun getDefaultValues(): Map<String, Any> {
        return mapOf(
            // Update config
            KEY_LATEST_VERSION to 1,
            KEY_UPDATE_URL to "https://play.google.com/store/apps/details?id=com.example.jazzlibraryktroomjpcompose",
            KEY_UPDATE_TITLE to "New Update Available",
            KEY_UPDATE_MESSAGE to "A new version is available. Please update.",
            KEY_MANDATORY_UPDATE to false,
            KEY_RELEASE_NOTES to "",

            // Feature flags
            KEY_ENABLE_NEW_FILTER to false,
            KEY_ENABLE_ANALYTICS to true,
            KEY_ENABLE_RATINGS to true,
            KEY_MAINTENANCE_MODE to false,
            KEY_MAINTENANCE_MESSAGE to "App is under maintenance.",

            // Content config
            KEY_API_BASE_URL to "https://api.example.com",
            KEY_CACHE_TIMEOUT to 3600000L,
            KEY_MAX_SEARCH_RESULTS to 100,
            KEY_ENABLE_OFFLINE to true
        )
    }
}