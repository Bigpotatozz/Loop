package com.oscar.orbimusic.presentation.models

import com.oscar.orbimusic.domain.models.Artist

sealed interface TrendingState {
    data object Loading : TrendingState
    data class Error(val message: String) : TrendingState
    data class Ready(val artists: List<Artist>) : TrendingState
}
