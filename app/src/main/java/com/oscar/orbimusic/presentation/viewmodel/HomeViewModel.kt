package com.oscar.orbimusic.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.domain.repository.DeezerRepository
import com.oscar.orbimusic.presentation.models.TopAlbumsState
import com.oscar.orbimusic.presentation.models.TopTracksState
import com.oscar.orbimusic.presentation.models.TrendingState
import com.oscar.orbimusic.presentation.player.PreviewPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val deezerRepository: DeezerRepository,
    private val context: Context
): ViewModel() {

    private val _trending = MutableStateFlow<TrendingState>(TrendingState.Loading);
    val trending: StateFlow<TrendingState> = _trending.asStateFlow();
    private val _topTracks = MutableStateFlow<TopTracksState>(TopTracksState.Loading);
    val topTracks: StateFlow<TopTracksState> = _topTracks.asStateFlow();

    private val _topAlbums = MutableStateFlow<TopAlbumsState>(TopAlbumsState.Loading);
    val topAlbums: StateFlow<TopAlbumsState> = _topAlbums.asStateFlow();

    //Pais de los charts, para titular "en México". Null mientras carga o si
    //falla: la pantalla cae al titulo sin pais.
    private val _chartCountry = MutableStateFlow<String?>(null);
    val chartCountry: StateFlow<String?> = _chartCountry.asStateFlow();

    //Mismo reproductor que usa la pantalla de album, con su propia instancia.
    //Cuando la URL caduca aqui se recarga el chart en vez del album.
    private val preview = PreviewPlayer(
        context = context,
        scope = viewModelScope,
        onNeedFreshUrl = { trackId -> refreshTracksAndGetUrl(trackId) }
    );

    val previewTrackId: StateFlow<Long?> = preview.previewTrackId;
    val isPreviewLoading: StateFlow<Boolean> = preview.isPreviewLoading;
    val previewError: StateFlow<String?> = preview.previewError;

    init {
        loadTrending();
        loadTopTracks();
        loadTopAlbums();
        loadChartCountry();
    }

    fun loadChartCountry() {
        viewModelScope.launch {
            deezerRepository.getChartCountry()
                .onSuccess { _chartCountry.value = it }
                .onFailure { Log.w("HomeViewModel", "Sin pais para los titulos", it) }
        }
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

    fun togglePreview(trackId: Long) {
        val url = (_topTracks.value as? TopTracksState.Ready)
            ?.tracks?.firstOrNull { it.id == trackId }?.previewUrl;
        preview.toggle(trackId, url);
    }

    fun stopPreview() = preview.stop();

    fun clearPreviewError() = preview.clearError();

    private suspend fun refreshTracksAndGetUrl(trackId: Long): String? {
        return deezerRepository.getTopTracks()
            .onSuccess { _topTracks.value = TopTracksState.Ready(it) }
            .getOrNull()
            ?.firstOrNull { it.id == trackId }
            ?.previewUrl;
    }

    override fun onCleared() {
        super.onCleared();
        preview.release();
    }
}
