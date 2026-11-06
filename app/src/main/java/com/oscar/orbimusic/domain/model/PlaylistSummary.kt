package com.oscar.orbimusic.domain.model

class PlaylistSummary(
    val id: Long?,
    val name: String,
    val songCount: Int,
    val coverPaths: List<String> = emptyList()
)

sealed interface PlaylistSelection {
    data object Favorites : PlaylistSelection
    data class Real(val id: Long) : PlaylistSelection
}
