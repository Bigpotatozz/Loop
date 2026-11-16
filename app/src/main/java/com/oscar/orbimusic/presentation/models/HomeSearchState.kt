package com.oscar.orbimusic.presentation.models

import com.oscar.orbimusic.domain.models.DeezerSearchResults

//Idle = no hay busqueda activa y el Home muestra sus secciones normales
sealed interface HomeSearchState {
    data object Idle : HomeSearchState
    data object Loading : HomeSearchState
    data class Error(val message: String) : HomeSearchState
    data class Ready(val query: String, val results: DeezerSearchResults) : HomeSearchState
}
