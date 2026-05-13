package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.AppUpdateInfo
import com.example.jazzlibraryktroomjpcompose.domain.models.FeatureFlags
import com.example.jazzlibraryktroomjpcompose.domain.models.ContentConfig
import kotlinx.coroutines.flow.Flow

/**
 * ✅ DOMAIN LAYER - Remote Config Repository
 */
interface RemoteConfigRepository {

    /**
     * Fetch and activate remote config values
     */
    suspend fun fetchAndActivate(): Result<Unit>

    /**
     * Get app update information
     */
    fun getAppUpdateInfo(): Flow<AppUpdateInfo>

    /**
     * Get feature flags
     */
    fun getFeatureFlags(): Flow<FeatureFlags>

    /**
     * Get content configuration
     */
    fun getContentConfig(): Flow<ContentConfig>

    /**
     * Get single boolean flag
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean

    /**
     * Get single string value
     */
    fun getString(key: String, default: String = ""): String

    /**
     * Get single int value
     */
    fun getInt(key: String, default: Int = 0): Int
}