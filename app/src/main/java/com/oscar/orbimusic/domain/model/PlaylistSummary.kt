package com.oscar.orbimusic.domain.model

class PlaylistSummary(
    val id: Long?,
    val name: String,
    val songCount: Int
)

sealed interface PlaylistSelection {
    data object Favorites : PlaylistSelection
    data class Real(val id: Long) : PlaylistSelection
}
