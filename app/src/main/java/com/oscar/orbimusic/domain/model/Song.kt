package com.oscar.orbimusic.domain.model

class Song(
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtPath: String? = null,
) {
}