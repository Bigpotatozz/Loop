package com.oscar.orbimusic.domain.models

//Resultado del buscador del Home: las tres busquedas de Deezer juntas
class DeezerSearchResults(
    val artists: List<Artist>,
    val albums: List<Album>,
    val tracks: List<Track>
) {
    val isEmpty: Boolean get() = artists.isEmpty() && albums.isEmpty() && tracks.isEmpty()
}
