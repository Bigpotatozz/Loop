package com.oscar.orbimusic.data.audio

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.session.PlaybackState.ACTION_PAUSE
import android.media.session.PlaybackState.ACTION_PLAY
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.io.File
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.oscar.orbimusic.MainActivity
import com.oscar.orbimusic.R
import com.oscar.orbimusic.data.audio.AudioPlayerService.Companion.ACTION_NEXT
import com.oscar.orbimusic.data.audio.AudioPlayerService.Companion.ACTION_PREVIOUS
import com.oscar.orbimusic.domain.model.Song

class AudioPlayerService: Service() {

    private lateinit var player: ExoPlayer //Inicializa expoPlayer
    private val binder = AudioPlayerBinder(); //Es una funcion que se expone para que el viewModel lo pueda usar
    private var currentSongs = listOf<Song>(); //Lista de canciones actuales
    private var currentIndex = 0; //Posicion de la cancion en reproduccion

    private lateinit var mediaSession: MediaSession; //Le indica a android que se esta reproduciendo audio
    private var onPlayerStateChangedListener: ((Boolean, Int) -> Unit)? = null

    fun setPlayerStateListener(listener: (Boolean, Int) -> Unit) {
        onPlayerStateChangedListener = listener
    }


    companion object {
        private const val CHANNEL_ID = "music_player_channel"; //Identificador del canal
        private const val NOTIFICATION_ID = 1; //Id de identificador

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
    }


    // __init__
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        initializePlayer();
        createNotificationChannel();
        initializeMediaSession();


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        //crea el canal de la notificacion
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Player",
            NotificationManager.IMPORTANCE_LOW
        )
        //Obtiene el servicio de notificaciones del sistema
        val manager = getSystemService(NotificationManager::class.java);
        //Agarra el servicio de notificaciones y le indica que cree un nuevo canal (el que previamente creamos)
        manager.createNotificationChannel(channel);
    }

    //Incializa el media session
    private fun initializeMediaSession(){
        mediaSession = MediaSession.Builder(this, player).build();
    }

    
    
    //Actualiza los datos de la notificacion (caratula, nombre, etc)
    @OptIn(UnstableApi::class)
    @SuppressLint("ForegroundServiceType")
    private fun updateNotification(isPlaying: Boolean){
        //Valida que no se este en la ultima cancion para no actualizar la notificacion
        if (currentIndex >= currentSongs.size) return
        //Obtiene la cancion por el indice en el que esta
        val song = currentSongs[currentIndex]

        //Es un intento que abre el MainActivity
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            //Abre MainActivity en una nueva tarea    Le indica que si ya hay una la reutilice
        }

        //Un pending intent sirve para guardar como el estado de la actividad para cuando el usuario la presione, mantiene los datos durante horas o dias
        // Se ejecuta el intento (notificacion) se mantiene aqui para que cuando el usuario de click se ejecute la accion (openAppIntent)
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE //Similar a openAppIntent
        )

        //Configuracion de la notificacion
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openAppPendingIntent)
            // Botón anterior
            .addAction(
                0, "Anterior",
                createPendingIntent(ACTION_PREVIOUS)
            )

            // Botón play/pausa
            .addAction(
                0,
                if (isPlaying) "Pausa" else "Play",
                createPendingIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY)
            )
            // Botón siguiente
            .addAction(
                0, "Siguiente",
                createPendingIntent(ACTION_NEXT)
            )
            .setStyle(MediaStyleNotificationHelper.MediaStyle(mediaSession)
                .setShowActionsInCompactView(0, 1, 2)  // Mostrar 3 acciones
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)  // Si está sonando, no se puede deslizar
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlayerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Detecta qué botón se presionó
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_NEXT  -> playNext()
            ACTION_PREVIOUS -> playPrevious()
        }
        return START_STICKY
    }

    //Inicializa el reproductor
    private fun initializePlayer(){
        //Crea el servicio de reproduccion
        player = ExoPlayer.Builder(this).build();
        //Agrega un listener a dicho servicio
        player.addListener(object: Player.Listener{

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Sincroniza el índice interno con el del reproductor
                currentIndex = player.currentMediaItemIndex
                updateNotification(player.isPlaying)
                onPlayerStateChangedListener?.invoke(player.isPlaying, currentIndex)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Actualiza la notificación cuando se pausa o reanuda
                updateNotification(isPlaying)
                onPlayerStateChangedListener?.invoke(isPlaying, currentIndex)
            }

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
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtPath?.let { Uri.fromFile(File(it)) })
                        .build()
                )
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
            player.seekTo(0)
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