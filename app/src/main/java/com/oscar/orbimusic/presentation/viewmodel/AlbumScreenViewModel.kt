package com.oscar.orbimusic.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.domain.repository.DeezerRepository
import com.oscar.orbimusic.presentation.models.AlbumState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlbumScreenViewModel(
    private val deezerRepository: DeezerRepository
): ViewModel() {

    private val _album = MutableStateFlow<AlbumState>(AlbumState.Loading);
    val album: StateFlow<AlbumState> = _album.asStateFlow();

    //El ultimo id pedido, para que retry() no necesite que se lo pasen
    private var currentAlbumId: Long? = null;

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
}
