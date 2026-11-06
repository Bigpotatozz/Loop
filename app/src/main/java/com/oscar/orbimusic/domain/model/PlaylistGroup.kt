package com.oscar.orbimusic.domain.model

//Un bloque de la vista agrupada: el nombre de la playlist y sus canciones.
//Favoritos se trata como una playlist mas, pero no existe en la tabla playlist,
//por eso su id es null. Esa distincion importa para las acciones que luego se
//agreguen: a Favoritos no se le puede cambiar el nombre ni borrar.
class PlaylistGroup(
    val id: Long?,
    val name: String,
    val songs: List<Song>
)
