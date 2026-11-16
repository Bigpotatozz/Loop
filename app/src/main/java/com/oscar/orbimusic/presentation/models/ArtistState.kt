package com.oscar.orbimusic.presentation.models

import com.oscar.orbimusic.domain.models.ArtistDetail

sealed interface ArtistState {
    data object Loading : ArtistState
    data class Error(val message: String) : ArtistState
    data class Ready(val artist: ArtistDetail) : ArtistState
}
