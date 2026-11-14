package com.oscar.orbimusic.data.network.dto

import com.google.gson.annotations.SerializedName

data class InfosDto(
    val country: String?, // "México", ya en el idioma del usuario
    @SerializedName("country_iso") val countryIso: String?, // "MX"
    val open: Boolean? // Si Deezer esta disponible en ese pais
)
