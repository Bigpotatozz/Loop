package com.oscar.orbimusic.data.network.dto

import com.google.gson.annotations.SerializedName

data class TrackDto(
    val id: Long?,
    val title: String?,
    val duration: Int?, // en segundos
    val link: String?,
    val preview: String?, // mp3 de 30s
    val position: Int?,
    val artist: ArtistDto?,
    val album: AlbumRefDto?
)

data class AlbumRefDto(
    val id: Long?,
    val title: String?,
    @SerializedName("cover_medium") val coverMedium: String?,
    @SerializedName("cover_xl") val coverXl: String?
)
