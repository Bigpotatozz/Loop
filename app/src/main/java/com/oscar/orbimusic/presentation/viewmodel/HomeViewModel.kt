package com.oscar.orbimusic.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.domain.repository.DeezerRepository
import com.oscar.orbimusic.presentation.models.TopAlbumsState
import com.oscar.orbimusic.presentation.models.TopTracksState
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
    private val _topTracks = MutableStateFlow<TopTracksState>(TopTracksState.Loading);
    val topTracks: StateFlow<TopTracksState> = _topTracks.asStateFlow();

    private val _topAlbums = MutableStateFlow<TopAlbumsState>(TopAlbumsState.Loading);
    val topAlbums: StateFlow<TopAlbumsState> = _topAlbums.asStateFlow();

    init {
        loadTrending();
        loadTopTracks();
        loadTopAlbums();
    }
    fun loadTrending() {
        viewModelScope.launch {
            _trending.value = TrendingState.Loading;

            deezerRepository.getTrendingArtists(limit = 10)
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

    fun loadTopTracks() {
        viewModelScope.launch {
            _topTracks.value = TopTracksState.Loading;

            deezerRepository.getTopTracks()
                .onSuccess { tracks ->
                    Log.d("HomeViewModel", "Top de canciones recibido: ${tracks.size}")
                    _topTracks.value = TopTracksState.Ready(tracks);
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "Fallo al traer el top de canciones", e)
                    _topTracks.value = TopTracksState.Error(
                        e.message ?: "No se pudo cargar el top de canciones"
                    );
                }
        }
    }

    fun loadTopAlbums() {
        viewModelScope.launch {
            _topAlbums.value = TopAlbumsState.Loading;

            deezerRepository.getTopAlbums()
                .onSuccess { albums ->
                    Log.d("HomeViewModel", "Top de albumes recibido: ${albums.size}")
                    _topAlbums.value = TopAlbumsState.Ready(albums);
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "Fallo al traer el top de albumes", e)
                    _topAlbums.value = TopAlbumsState.Error(
                        e.message ?: "No se pudo cargar el top de álbumes"
                    );
                }
        }
    }
}
