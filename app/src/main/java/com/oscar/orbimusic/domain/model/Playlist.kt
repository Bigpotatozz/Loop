package com.oscar.orbimusic.domain.model

//Modelo de dominio. Los nombres van en ingles para ir parejo con Song; el
//mapeo contra nombre/descripcion de PlaylistEntity vive en el repositorio,
//que es justo para lo que existe esa capa.
class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String,
    val songCount: Int = 0,
    val createdAt: Long = 0
)
