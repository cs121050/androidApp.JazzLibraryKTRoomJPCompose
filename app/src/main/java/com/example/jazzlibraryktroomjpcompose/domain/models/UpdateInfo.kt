package com.example.jazzlibraryktroomjpcompose.domain.models

data class UpdateInfo(
    val latestVersionCode: Int,
    val forceMinVersionCode: Int,
    val downloadUrl: String,
    val changeLog: String,
    val lastUpdateTimestamp: Long
)