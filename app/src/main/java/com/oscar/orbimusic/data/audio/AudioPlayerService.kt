package com.oscar.orbimusic.data.audio

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.oscar.orbimusic.domain.model.Song

class AudioPlayerService: Service() {

    private lateinit var player: ExoPlayer //Inicializa expoPlayer
    private val binder = AudioPlayerBinder(); //Es una funcion que se expone para que el viewModel lo pueda usar
    private var currentSongs = listOf<Song>(); //Lista de canciones actuales
    private var currentIndex = 0; //Posicion de la cancion en reproduccion

    // __init__
    override fun onCreate() {
        super.onCreate()
        initializePlayer();
    }


    //Inicializa el reproductor
    private fun initializePlayer(){
        //Crea el servicio de reproduccion
        player = ExoPlayer.Builder(this).build();
        //Agrega un listener a dicho servicio
        player.addListener(object: Player.Listener{
            //Metodo que se ejecuta cada vez que el estado cambia (START, PAUSE, NEXT, ETC)
            override fun onPlaybackStateChanged(state: Int){
                when (state) {
                    //Si la cancion termina reproduce la siguiente
                    Player.STATE_ENDED -> playNext()
                    //Aqui se pueden meter mas condicionales
                    else -> {}

                }
            }
        })
    }


    fun setPlaylist(songs: List<Song>, startIndex: Int = 0){
        currentSongs = songs //Lista de canciones
        currentIndex = startIndex //Indice de inicio

        //A cada cancion la hace un "mediaItem" que es un tipo de dato que solo entiende el servicio de reproduccion
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.path)
                .build()
        }
        //Le indica a expoLayer cuales son las canciones a reproducir
        player.setMediaItems(mediaItems, startIndex, 0)
        //Prepara el servicio de reproduccion
        player.prepare()

    }

    //Reproduce el archivo
    fun play() {
        player.play()
        Log.d("AudioService", "Reproduciendo")
    }

    //Pausa el archivo
    fun pause() {
        player.pause()
        Log.d("AudioService", "Pausado")
    }

    //Reproduce la siguiente cancion
    fun playNext() {
        //Si el indice es mejor al total de canciones - 1
        if (currentIndex < currentSongs.size - 1) {
            //Suma 1 al indice
            currentIndex++

            //El servicio salta a un archivo especifico
            player.seekToDefaultPosition(currentIndex)
            //Reproduce el archivo
            player.play()
            Log.d("AudioService", "Siguiente canción: $currentIndex")
        }
    }

    //Reproduce la cancion anterior
    fun playPrevious() {
        //Si el indice es mayor a 0
        if (currentIndex > 0) {
            //Resta uno
            currentIndex--
            //Salta a ese indice
            player.seekToDefaultPosition(currentIndex)
            //Reproduce
            player.play()
        } else if (player.currentPosition > 3000) {
            // Si está al principio pero pasó 3 segundos, vuelve al inicio
            player.seekTo(0)
        }
    }

    //Va a una posicion en especifico
    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    //Funciones que guardan los estados del servicio de reproduccion
    fun isPlaying(): Boolean = player.isPlaying
    fun getCurrentPosition(): Long = player.currentPosition
    fun getDuration(): Long = player.duration
    fun getCurrentIndex(): Int = currentIndex


    override fun onBind(intent: Intent?): IBinder = binder

    //Acciones que se ejecutan al destruir el servicio
    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }




}