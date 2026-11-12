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
    val artist: ArtistDto?,
    //Estos solo vienen en album/{id}. En chart/0/albums se quedan en null.
    @SerializedName("release_date") val releaseDate: String?, // "yyyy-MM-dd"
    @SerializedName("nb_tracks") val nbTracks: Int?,
    val duration: Int?, // en segundos, el total del album
    val tracks: DeezerListDto<TrackDto>?
)
