package com.oscar.orbimusic.presentation.models

import com.oscar.orbimusic.domain.models.AlbumDetail

//Mismo patron que TopAlbumsState.
sealed interface AlbumState {
    data object Loading : AlbumState
    data class Error(val message: String) : AlbumState
    data class Ready(val album: AlbumDetail) : AlbumState
}
