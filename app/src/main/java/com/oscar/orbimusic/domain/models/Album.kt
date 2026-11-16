package com.oscar.orbimusic.domain.models

class Album(
    val id: Long,
    val title: String,
    val artistName: String,
    val coverMedium: String,
    val coverXl: String,
    val position: Int? = null,
    val releaseDate: String? = null, // "yyyy-MM-dd"
    val recordType: String? = null
)
