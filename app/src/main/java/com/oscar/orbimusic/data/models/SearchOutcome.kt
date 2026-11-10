package com.oscar.orbimusic.data.models

data class SearchOutcome(
    val results: List<YtSearchResult>,
    val fallbackFrom: SearchSource? = null
)
