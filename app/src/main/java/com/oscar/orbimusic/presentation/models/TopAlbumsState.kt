package com.oscar.orbimusic.presentation.models

import com.oscar.orbimusic.domain.models.Album

//Mismo patron que TrendingState y TopTracksState.
sealed interface TopAlbumsState {
    data object Loading : TopAlbumsState
    data class Error(val message: String) : TopAlbumsState
    data class Ready(val albums: List<Album>) : TopAlbumsState
}
