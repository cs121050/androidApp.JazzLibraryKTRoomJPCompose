// app/src/main/java/com/example/jazzlibraryktroomjpcompose/data/TosPolicyManager.kt

package com.example.jazzlibraryktroomjpcompose.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tos_preferences")

class TosPolicyManager(private val context: Context) {

    companion object {
        private val TOS_ACCEPTED_KEY = booleanPreferencesKey("tos_accepted")
        private val PRIVACY_POLICY_ACCEPTED_KEY = booleanPreferencesKey("privacy_policy_accepted")
        private val YOUTUBE_TOS_ACCEPTED_KEY = booleanPreferencesKey("youtube_tos_accepted")
        private val TOS_ACCEPTANCE_DATE_KEY = longPreferencesKey("tos_acceptance_date")
    }

    val isTosAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TOS_ACCEPTED_KEY] ?: false
        }

    val isPrivacyPolicyAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PRIVACY_POLICY_ACCEPTED_KEY] ?: false
        }

    val isYoutubeTosAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[YOUTUBE_TOS_ACCEPTED_KEY] ?: false
        }

    suspend fun acceptAllPolicies() {
        context.dataStore.edit { preferences ->
            preferences[TOS_ACCEPTED_KEY] = true
            preferences[PRIVACY_POLICY_ACCEPTED_KEY] = true
            preferences[YOUTUBE_TOS_ACCEPTED_KEY] = true
            preferences[TOS_ACCEPTANCE_DATE_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun rejectPolicies() {
        context.dataStore.edit { preferences ->
            preferences[TOS_ACCEPTED_KEY] = false
            preferences[PRIVACY_POLICY_ACCEPTED_KEY] = false
            preferences[YOUTUBE_TOS_ACCEPTED_KEY] = false
        }
    }

    suspend fun resetAcceptance() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOS_ACCEPTED_KEY)
            preferences.remove(PRIVACY_POLICY_ACCEPTED_KEY)
            preferences.remove(YOUTUBE_TOS_ACCEPTED_KEY)
            preferences.remove(TOS_ACCEPTANCE_DATE_KEY)
        }
    }
}