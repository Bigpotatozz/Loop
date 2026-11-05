package com.oscar.orbimusic.data.models

data class YtSearchResult(
    val id: String,
    val title: String,
    val channel: String,
    val durationSeconds: Long,
    val thumbnailUrl: String,
    val url: String
)

