package com.oscar.orbimusic.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.domain.repository.DeezerRepository
import com.oscar.orbimusic.presentation.models.ArtistState
import com.oscar.orbimusic.presentation.player.PreviewPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtistScreenViewModel(
    private val deezerRepository: DeezerRepository,
    private val context: Context
): ViewModel() {

    private val _artist = MutableStateFlow<ArtistState>(ArtistState.Loading);
    val artist: StateFlow<ArtistState> = _artist.asStateFlow();

    private var currentArtistId: Long? = null;

    private val preview = PreviewPlayer(
        context = context,
        scope = viewModelScope,
        onNeedFreshUrl = { trackId -> refreshArtistAndGetUrl(trackId) }
    );

    val previewTrackId: StateFlow<Long?> = preview.previewTrackId;
    val isPreviewLoading: StateFlow<Boolean> = preview.isPreviewLoading;
    val previewError: StateFlow<String?> = preview.previewError;

    fun load(artistId: Long) {
        if (artistId == currentArtistId && _artist.value is ArtistState.Ready) return;
        currentArtistId = artistId;

        viewModelScope.launch {
            _artist.value = ArtistState.Loading;

            deezerRepository.getArtist(artistId, TOP_LIMIT)
                .onSuccess { artist ->
                    Log.d("ArtistScreenViewModel", "Artista recibido: ${artist.name} (${artist.topTracks.size} populares)")
                    _artist.value = ArtistState.Ready(artist);
                }
                .onFailure { e ->
                    Log.e("ArtistScreenViewModel", "Fallo al traer el artista $artistId", e)
                    _artist.value = ArtistState.Error(
                        e.message ?: "No se pudo cargar el artista"
                    );
                }
        }
    }

    fun retry() {
        val id = currentArtistId ?: return;
        _artist.value = ArtistState.Loading;
        load(id);
    }

    fun togglePreview(trackId: Long) {
        val url = (_artist.value as? ArtistState.Ready)
            ?.artist?.topTracks?.firstOrNull { it.id == trackId }?.previewUrl;
        preview.toggle(trackId, url);
    }

    fun stopPreview() = preview.stop();

    fun clearPreviewError() = preview.clearError();

    private suspend fun refreshArtistAndGetUrl(trackId: Long): String? {
        val id = currentArtistId ?: return null;

        return deezerRepository.getArtist(id, TOP_LIMIT)
            .onSuccess { _artist.value = ArtistState.Ready(it) }
            .getOrNull()
            ?.topTracks
            ?.firstOrNull { it.id == trackId }
            ?.previewUrl;
    }

    companion object {
        const val TOP_LIMIT = 10;
    }

    override fun onCleared() {
        super.onCleared();
        preview.release();
    }
}
