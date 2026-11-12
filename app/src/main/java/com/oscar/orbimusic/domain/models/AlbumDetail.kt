package com.oscar.orbimusic.domain.models

//El album completo, con sus canciones. Album se queda para las tarjetas del
//Home, que no necesitan nada de esto.
class AlbumDetail(
    val id: Long,
    val title: String,
    val artistName: String,
    val coverXl: String,
    val releaseDate: String? = null,
    val recordType: String? = null,
    val durationSeconds: Int? = null,
    //Cada Track lleva la caratula del propio album: dentro de album/{id} las
    //canciones no traen la suya
    val tracks: List<Track>
)
