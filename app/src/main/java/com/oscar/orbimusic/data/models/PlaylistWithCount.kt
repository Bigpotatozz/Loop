package com.oscar.orbimusic.data.models

import androidx.room.Embedded
import com.oscar.orbimusic.data.local.database.entities.PlaylistEntity


data class PlaylistWithCount(

//Embedded es un decorador para room que le dice que interprete los atributos de la clase como columnas
    @Embedded val playlist: PlaylistEntity,
    val songCount: Int
)
