package com.oscar.orbimusic.data.models
enum class SearchSource(val label: String) {
    YT_MUSIC("YouTube Music"),
    YOUTUBE("YouTube");

    fun other(): SearchSource = if (this == YT_MUSIC) YOUTUBE else YT_MUSIC
}
