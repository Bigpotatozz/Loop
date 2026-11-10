package com.oscar.orbimusic.data.network.dto

import com.google.gson.annotations.SerializedName

data class AlbumDto(
    val id: Long?,
    val title: String?,
    val link: String?,
    val position: Int?,
    @SerializedName("record_type") val recordType: String?,
    val tracklist: String?,
    @SerializedName("cover_medium") val coverMedium: String?,
    @SerializedName("cover_xl") val coverXl: String?,
    val artist: ArtistDto?
)
