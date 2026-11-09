package com.oscar.orbimusic.data.audio

import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class AudioPlayerService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    //Por aqui entra el MediaController del ViewModel. Sustituye a onBind/Binder.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    //Sin esto la notificacion se queda pegada al cerrar la app desde recientes
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        //Hay que soltar las dos cosas: la sesion y el player que envuelve
        mediaSession.run {
            player.release()
            release()
        }
        super.onDestroy()
    }
}
