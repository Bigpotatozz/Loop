package com.oscar.orbimusic.presentation.models

import com.oscar.orbimusic.domain.models.Track

//Mismo patron que TrendingState: la pantalla pinta uno de los tres casos sin
//tener que combinar una lista vacia con un booleano de carga.
sealed interface TopTracksState {
    data object Loading : TopTracksState
    data class Error(val message: String) : TopTracksState
    data class Ready(val tracks: List<Track>) : TopTracksState
}
