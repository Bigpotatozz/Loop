package com.oscar.orbimusic.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.oscar.orbimusic.domain.repository.DeezerRepository
import com.oscar.orbimusic.presentation.models.AlbumState
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

    //Que cancion esta sonando en preview. Null = ninguna.
    private val _previewTrackId = MutableStateFlow<Long?>(null);
    val previewTrackId: StateFlow<Long?> = _previewTrackId.asStateFlow();

    //True mientras ExoPlayer baja los primeros bytes del mp3
    private val _isPreviewLoading = MutableStateFlow(false);
    val isPreviewLoading: StateFlow<Boolean> = _isPreviewLoading.asStateFlow();

    private val _previewError = MutableStateFlow<String?>(null);
    val previewError: StateFlow<String?> = _previewError.asStateFlow();

    //Para no entrar en bucle: tras un 403 se refresca el album una sola vez
    private var retriedExpiredUrl = false;

    //Un ExoPlayer propio y no el de AudioPlayerService: el preview no debe
    //tocar la cola ni aparecer en la notificacion. Lazy para no crearlo si
    //nunca se toca una fila.
    private val previewPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> _isPreviewLoading.value = true
                        Player.STATE_READY -> _isPreviewLoading.value = false
                        //Terminaron los 30s: la fila vuelve a su numero
                        Player.STATE_ENDED -> stopPreview()
                        else -> Unit
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("AlbumScreenViewModel", "Fallo el preview", error)
                    val trackId = _previewTrackId.value

                    //La URL caduca a los ~15 min y Deezer contesta 403. Se
                    //vuelve a pedir el album (URLs nuevas) y se reintenta.
                    if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS &&
                        trackId != null && !retriedExpiredUrl
                    ) {
                        retriedExpiredUrl = true
                        refreshAndReplay(trackId)
                        return
                    }

                    stopPreview()
                    _previewError.value = "No se pudo reproducir el preview"
                }
            })
        }
    }

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

    //Tocar la fila que ya suena la detiene; tocar otra cambia a esa
    fun togglePreview(trackId: Long) {
        if (_previewTrackId.value == trackId) {
            stopPreview();
            return;
        }
        retriedExpiredUrl = false;
        playPreview(trackId);
    }

    fun stopPreview() {
        _previewTrackId.value = null;
        _isPreviewLoading.value = false;
        //Si nunca se creo el player no hay nada que detener
        if (previewStarted) previewPlayer.stop();
    }

    fun clearPreviewError() {
        _previewError.value = null;
    }

    //Para que stopPreview() no fuerce a crear el ExoPlayer solo para pararlo
    private var previewStarted = false;

    private fun playPreview(trackId: Long) {
        val album = (_album.value as? AlbumState.Ready)?.album ?: return;
        val url = album.tracks.firstOrNull { it.id == trackId }?.previewUrl ?: return;

        previewStarted = true;
        _previewTrackId.value = trackId;
        _isPreviewLoading.value = true;

        previewPlayer.setMediaItem(MediaItem.fromUri(url));
        previewPlayer.prepare();
        previewPlayer.play();
    }

    //Vuelve a pedir el album sin pasar por Loading (la lista no parpadea) y
    //retoma la misma cancion con su URL nueva
    private fun refreshAndReplay(trackId: Long) {
        val id = currentAlbumId ?: return;

        viewModelScope.launch {
            deezerRepository.getAlbum(id)
                .onSuccess { album ->
                    _album.value = AlbumState.Ready(album);
                    //Si mientras tanto el usuario la detuvo, no se reanuda
                    if (_previewTrackId.value == trackId) playPreview(trackId);
                }
                .onFailure {
                    stopPreview();
                    _previewError.value = "No se pudo reproducir el preview";
                }
        }
    }

    override fun onCleared() {
        super.onCleared();
        if (previewStarted) previewPlayer.release();
    }
}
