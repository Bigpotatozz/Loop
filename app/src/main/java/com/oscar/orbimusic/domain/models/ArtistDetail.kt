package com.oscar.orbimusic.domain.models

class ArtistDetail(
    val id: Long,
    val name: String,
    val pictureXl: String,
    val fanCount: Int? = null,
    val albumCount: Int? = null,
    val topTracks: List<Track>
)
