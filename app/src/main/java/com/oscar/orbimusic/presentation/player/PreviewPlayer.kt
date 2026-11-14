package com.oscar.orbimusic.presentation.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PreviewPlayer(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onNeedFreshUrl: suspend (Long) -> String?
) {

    //Que cancion esta sonando en preview. Null = ninguna.
    private val _previewTrackId = MutableStateFlow<Long?>(null)
    val previewTrackId: StateFlow<Long?> = _previewTrackId.asStateFlow()

    //True mientras ExoPlayer baja los primeros bytes del mp3
    private val _isPreviewLoading = MutableStateFlow(false)
    val isPreviewLoading: StateFlow<Boolean> = _isPreviewLoading.asStateFlow()

    private val _previewError = MutableStateFlow<String?>(null)
    val previewError: StateFlow<String?> = _previewError.asStateFlow()

    //Para no entrar en bucle: tras un 403 se refresca la URL una sola vez
    private var retriedExpiredUrl = false

    //Para que stop() no fuerce a crear el ExoPlayer solo para pararlo
    private var started = false

    //Lazy para no crearlo si el usuario nunca toca una cancion
    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> _isPreviewLoading.value = true
                        Player.STATE_READY -> _isPreviewLoading.value = false
                        //Terminaron los 30s: la tarjeta vuelve a su estado normal
                        Player.STATE_ENDED -> stop()
                        else -> Unit
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("PreviewPlayer", "Fallo el preview", error)
                    val trackId = _previewTrackId.value

                    if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS &&
                        trackId != null && !retriedExpiredUrl
                    ) {
                        retriedExpiredUrl = true
                        refreshAndReplay(trackId)
                        return
                    }

                    stop()
                    _previewError.value = "No se pudo reproducir el preview"
                }
            })
        }
    }

    //Tocar la que ya suena la detiene; tocar otra cambia a esa
    fun toggle(trackId: Long, url: String?) {
        if (_previewTrackId.value == trackId) {
            stop()
            return
        }
        retriedExpiredUrl = false
        play(trackId, url ?: return)
    }

    fun stop() {
        _previewTrackId.value = null
        _isPreviewLoading.value = false
        if (started) player.stop()
    }

    fun clearError() {
        _previewError.value = null
    }

    fun release() {
        if (started) player.release()
    }

    private fun play(trackId: Long, url: String) {
        started = true
        _previewTrackId.value = trackId
        _isPreviewLoading.value = true

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    private fun refreshAndReplay(trackId: Long) {
        scope.launch {
            val fresh = onNeedFreshUrl(trackId)

            //Si mientras se pedia la URL nueva el usuario lo detuvo, no se reanuda
            if (fresh == null || _previewTrackId.value != trackId) {
                stop()
                if (fresh == null) _previewError.value = "No se pudo reproducir el preview"
                return@launch
            }

            play(trackId, fresh)
        }
    }
}
