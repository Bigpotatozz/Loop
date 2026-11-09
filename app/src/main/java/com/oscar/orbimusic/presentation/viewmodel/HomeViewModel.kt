package com.oscar.orbimusic.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.domain.repository.DeezerRepository
import com.oscar.orbimusic.presentation.models.TrendingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val deezerRepository: DeezerRepository
): ViewModel() {

    private val _trending = MutableStateFlow<TrendingState>(TrendingState.Loading);
    val trending: StateFlow<TrendingState> = _trending.asStateFlow();

    init {
        loadTrending();
    }
    fun loadTrending() {
        viewModelScope.launch {
            _trending.value = TrendingState.Loading;

            deezerRepository.getTrendingArtists()
                .onSuccess { artists ->
                    Log.d("HomeViewModel", "Tendencias recibidas: ${artists.size}")
                    artists.forEach {
                        Log.d("HomeViewModel", "  ${it.position}. ${it.name} (${it.id})")
                    }

                    _trending.value = TrendingState.Ready(artists);
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "Fallo al traer tendencias", e)
                    _trending.value = TrendingState.Error(
                        e.message ?: "No se pudieron cargar las tendencias"
                    );
                }
        }
    }
}
