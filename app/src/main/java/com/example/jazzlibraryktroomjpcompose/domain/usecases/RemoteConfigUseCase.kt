package com.example.jazzlibraryktroomjpcompose.domain.usecases

import android.util.Log
import com.example.jazzlibraryktroomjpcompose.domain.models.AppUpdateInfo
import com.example.jazzlibraryktroomjpcompose.domain.models.FeatureFlags
import com.example.jazzlibraryktroomjpcompose.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ DOMAIN LAYER - Remote Config Use Case
 * Pure business logic for remote config
 */
@Singleton
class RemoteConfigUseCase @Inject constructor(
    private val repository: RemoteConfigRepository
) {

    companion object {
        private const val TAG = "RemoteConfigUseCase"
    }

    /**
     * Fetch remote config and check for updates
     */
    suspend fun fetchRemoteConfig(): Result<Unit> {
        Log.d(TAG, "Fetching remote config...")
        return repository.fetchAndActivate()
    }

    /**
     * Get app update information and check if update is needed
     */
    fun getUpdateInfo(): Flow<AppUpdateInfo> {
        return repository.getAppUpdateInfo()
    }

    /**
     * Check if mandatory update is available
     */
    suspend fun isMandatoryUpdateAvailable(): Boolean {
        val updateInfo = repository.getAppUpdateInfo()
        var result = false
        updateInfo.collect { info ->
            result = info.isMandatory && info.latestVersionCode > info.currentVersionCode
        }
        return result
    }

    /**
     * Check if optional update is available
     */
    suspend fun isOptionalUpdateAvailable(): Boolean {
        val updateInfo = repository.getAppUpdateInfo()
        var result = false
        updateInfo.collect { info ->
            result = !info.isMandatory && info.latestVersionCode > info.currentVersionCode
        }
        return result
    }

    /**
     * Get feature flags
     */
    fun getFeatureFlags(): Flow<FeatureFlags> {
        return repository.getFeatureFlags()
    }

    /**
     * Check if feature is enabled
     */
    suspend fun isFeatureEnabled(featureName: String): Boolean {
        return when (featureName) {
            "new_filter" -> {
                var enabled = false
                repository.getFeatureFlags().collect { flags ->
                    enabled = flags.enableNewFilter
                }
                enabled
            }
            "analytics" -> {
                var enabled = false
                repository.getFeatureFlags().collect { flags ->
                    enabled = flags.enableAnalytics
                }
                enabled
            }
            "ratings" -> {
                var enabled = false
                repository.getFeatureFlags().collect { flags ->
                    enabled = flags.enableRatings
                }
                enabled
            }
            "maintenance" -> {
                var enabled = false
                repository.getFeatureFlags().collect { flags ->
                    enabled = flags.maintenanceMode
                }
                enabled
            }
            else -> false
        }
    }
}