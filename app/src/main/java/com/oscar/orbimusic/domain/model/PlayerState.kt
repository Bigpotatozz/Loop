package com.oscar.orbimusic.domain.model


data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,  // en milisegundos
    val duration: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0,
)