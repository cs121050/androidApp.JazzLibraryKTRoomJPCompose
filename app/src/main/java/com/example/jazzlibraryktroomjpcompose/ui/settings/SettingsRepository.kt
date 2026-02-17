package com.example.jazzlibraryktroomjpcompose.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    //default values of the  flags
    val darkMode: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SettingsKeys.DARK_MODE] ?: true // default value
        }
    // we are using flow, because thats what dataStore need in order to make performe theUI change imidiatly
    val randomiseVideoList: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SettingsKeys.RANDOMISE_VIDEO_LIST_CONTENT] ?: true // default value
        }

    //setter functions
    suspend fun setdarkMode(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.DARK_MODE] = value
        }
    }

    suspend fun setVideoListRandomisation(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.RANDOMISE_VIDEO_LIST_CONTENT] = value
        }
    }
}