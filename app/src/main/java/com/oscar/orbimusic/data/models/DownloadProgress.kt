package com.oscar.orbimusic.data.models

data class DownloadProgress(
    val isCompleted: Boolean = false,
    val error: String? = null,
    val downloadedPath: String? = null,
    val progress: Int = 0
)