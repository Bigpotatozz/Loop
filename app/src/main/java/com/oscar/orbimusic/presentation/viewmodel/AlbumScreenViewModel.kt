package com.oscar.orbimusic.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.domain.repository.DeezerRepository
import com.oscar.orbimusic.presentation.models.AlbumState
import com.oscar.orbimusic.presentation.player.PreviewPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlbumScreenViewModel(
    private val deezerRepository: DeezerRepository,
    private val context: Context
): ViewModel() {

    private val _album = MutableStateFlow<AlbumState>(AlbumState.Loading);
    val album: StateFlow<AlbumState> = _album.asStateFlow();

    //El ultimo id pedido, para que retry() no necesite que se lo pasen
    private var currentAlbumId: Long? = null;

    //El ExoPlayer y el manejo de URLs caducadas viven en PreviewPlayer, que
    //comparte con el Home. Aqui solo se dice como conseguir una URL nueva:
    //volver a pedir el album y sacar la de esa cancion.
    private val preview = PreviewPlayer(
        context = context,
        scope = viewModelScope,
        onNeedFreshUrl = { trackId -> refreshAlbumAndGetUrl(trackId) }
    );

    //Se reexponen con los mismos nombres de antes para no tocar AlbumScreen
    val previewTrackId: StateFlow<Long?> = preview.previewTrackId;
    val isPreviewLoading: StateFlow<Boolean> = preview.isPreviewLoading;
    val previewError: StateFlow<String?> = preview.previewError;

    fun load(albumId: Long) {
        //Si ya esta cargado ese mismo album no se vuelve a pedir: al regresar a
        //la pantalla el LaunchedEffect corre otra vez
        if (albumId == currentAlbumId && _album.value is AlbumState.Ready) return;
        currentAlbumId = albumId;

        viewModelScope.launch {
            _album.value = AlbumState.Loading;

            deezerRepository.getAlbum(albumId)
                .onSuccess { album ->
                    Log.d("AlbumScreenViewModel", "Album recibido: ${album.title} (${album.tracks.size} canciones)")
                    _album.value = AlbumState.Ready(album);
                }
                .onFailure { e ->
                    Log.e("AlbumScreenViewModel", "Fallo al traer el album $albumId", e)
                    _album.value = AlbumState.Error(
                        e.message ?: "No se pudo cargar el álbum"
                    );
                }
        }
    }

    fun retry() {
        val id = currentAlbumId ?: return;
        _album.value = AlbumState.Loading;
        load(id);
    }

    fun togglePreview(trackId: Long) {
        val url = (_album.value as? AlbumState.Ready)
            ?.album?.tracks?.firstOrNull { it.id == trackId }?.previewUrl;
        preview.toggle(trackId, url);
    }

    fun stopPreview() = preview.stop();

    fun clearPreviewError() = preview.clearError();

  
    private suspend fun refreshAlbumAndGetUrl(trackId: Long): String? {
        val id = currentAlbumId ?: return null;

        return deezerRepository.getAlbum(id)
            .onSuccess { _album.value = AlbumState.Ready(it) }
            .getOrNull()
            ?.tracks
            ?.firstOrNull { it.id == trackId }
            ?.previewUrl;
    }

    override fun onCleared() {
        super.onCleared();
        preview.release();
    }
}
