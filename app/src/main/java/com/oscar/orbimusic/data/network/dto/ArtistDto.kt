package com.oscar.orbimusic.data.network.dto

import com.google.gson.annotations.SerializedName

data class ArtistDto(

    val id: Long?,
    val name: String?,
    val link: String?, // url de deezer
    val share: String?, // Lo mismo que el link pero con parametros de analitica
    val picture: String?,
    //Las cuatro medidas reales. Los tamanos vienen en la propia URL:
    @SerializedName("picture_small") val pictureSmall: String?,
    @SerializedName("picture_medium") val pictureMedium: String?,
    @SerializedName("picture_big") val pictureBig: String?,
    @SerializedName("picture_xl") val pictureXl: String?,
    @SerializedName("nb_album") val nbAlbum: Int?, // Numero de albumes del artista
    @SerializedName("nb_fan") val nbFan: Int?,
    val radio: Boolean?, // Si tiene radio en deezer
    val tracklist: String?,
    val position: Int?,
    val type: String?
)
