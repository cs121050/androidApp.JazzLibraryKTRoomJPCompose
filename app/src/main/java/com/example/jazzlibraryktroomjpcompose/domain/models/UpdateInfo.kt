package com.example.jazzlibraryktroomjpcompose.domain.models

data class UpdateInfo(
    val latestVersion: String,
    val forceMinVersion: String,
    val downloadUrl: String,
    val changeLog: String,
    val lastUpdateTimestamp: Long
)