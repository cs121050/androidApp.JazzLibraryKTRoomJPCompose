package com.example.jazzlibraryktroomjpcompose.data.remote.models

import com.google.gson.annotations.SerializedName

data class SyncPage<T>(
    val items: List<T> = emptyList(),
    @SerializedName("nextCursor")
    val nextCursor: String? = null,
    @SerializedName("hasMore")
    val hasMore: Boolean = false,
    val count: Int = 0
)

data class SyncResource(
    val name: String,
    val path: String,
    val count: Long
)