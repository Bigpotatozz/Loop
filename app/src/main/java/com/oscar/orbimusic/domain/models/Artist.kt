package com.oscar.orbimusic.domain.models


class Artist(
    val id: Long,
    val name: String,
    val pictureMedium: String,
    val pictureXl: String,
    val fanCount: Int? = null,
    val albumCount: Int? = null,
    val position: Int? = null
)
