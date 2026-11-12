package com.oscar.orbimusic.domain.models



class Track(
    val id: Long,
    val title: String,
    val artistName: String,
    val coverMedium: String,
    val coverXl: String,
    val durationSeconds: Int? = null,
    val position: Int? = null,
    val previewUrl: String? = null // URL de reproduccion de Deezer
)
