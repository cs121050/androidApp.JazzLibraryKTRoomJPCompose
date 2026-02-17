package com.example.jazzlibraryktroomjpcompose.ui.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val SORT_ORDER = stringPreferencesKey("sort_order")

    val RANDOMISE_VIDEO_LIST_CONTENT = booleanPreferencesKey("randomise_video_list")

}