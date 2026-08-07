package com.oscar.orbimusic.data.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaMetadataRetriever
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.R
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.log


@UnstableApi
class DownloadMusicService: Service() {

    //Inicializa el binder para que el viewModel se pueda conectar
    private val binder =  DownloadServiceBinder()
    //Establece el hilo que se usara para las descargas, se usa SupervisorJob para que las otras acciones continuen apesar de que falle una previa
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO);


    // Es un callback que modificable donde se indicara que pasara cuando exista un error o se descargue una cancion correctamente
    private var downloadListener: ((DownloadProgress) -> Unit)? = null;

    // Variable para rastrear el estado de la inicialización y evitar el error "instance not initialized"
    private val isInitialized = CompletableDeferred<Boolean>()

    companion object {
        //Canal para la notificacion
        private const val CHANNEL_ID = "music_download_channel"
        // ID de la notificación
        private const val NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel();
        initializeYtDL();
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Crea un canal con prioridad baja (no hace ruido ni vibra)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }


    //Funcion que inicializa y actualiza la libreria de YoutubeDL
    private fun initializeYtDL(){
        scope.launch {
            try{
                YoutubeDL.getInstance().init(applicationContext)
                FFmpeg.getInstance().init(applicationContext)
                // Se marca como inicializado después de init()
                isInitialized.complete(true)

                YoutubeDL.getInstance().updateYoutubeDL(this@DownloadMusicService)
                Log.d("DownloadService", "YouTubeDL initialized and updated")
            }catch (e: YoutubeDLException){
                Log.e("DownloadService", "$e", e)
                if (!isInitialized.isCompleted) isInitialized.complete(false)
            } catch (e: Exception) {
                Log.e("DownloadService", "$e", e)
                if (!isInitialized.isCompleted) isInitialized.complete(false)
            }
        }
    }

    // Permite que el ViewModel se entere de los cambios de descarga
    @OptIn(UnstableApi::class)
    fun setDownloadListener(listener: (DownloadProgress) -> Unit) {
        downloadListener = listener
    }


    fun downloadFromYt(youtubeUrl: String, title: String){
        scope.launch {

            try {
                // Espera a que la inicialización termine antes de continuar para evitar IllegalStateException
                if (!isInitialized.await()) {
                    throw Exception("YouTubeDL failed to initialize")
                }

                updateNotification("Descargando: $title", 0);

                val musicDir = getMusicDirectory();
                Log.d("DownloadService", "Descargando a: ${musicDir.absolutePath}");

                val request = YoutubeDLRequest(youtubeUrl).apply {
                    addOption("-f", "bestaudio[ext=m4a]/best");
                    addOption("-x")
                    addOption("--audio-format", "m4a")
                    addOption("--audio-quality", "0")
                    addOption("--add-metadata")
                    addOption("--embed-metadata")
                    addOption("--write-thumbnail") //Obtiene la imagen
                    addOption("--convert-thumbnail", "jpg") //Guarda la imagen en jpg
                    addOption("-o", "${musicDir.absolutePath}/%(title)s.%(ext)s")

                }

                //Eejcuta la descarga
                Log.d("DownloadService", "Iniciando descarga con opciones de metadatos embebidos")
                val result = YoutubeDL.getInstance().execute(request);

                if (result.exitCode == 0){

                    val filesDownloaded = musicDir.listFiles(){file ->
                        file.extension == "m4a"
                    }?.sortedByDescending { it.lastModified() }

                    if(!filesDownloaded.isNullOrEmpty()){
                        val downloadedAudio = filesDownloaded.first();

                        //Herramienta que te permite extraer metadata
                        val retriever = MediaMetadataRetriever();
                        retriever.setDataSource(downloadedAudio.path); //Asignamos el archivo
                        val audioTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) //obtenemos el titulo
                        retriever.release(); //Ejecutamos la accion

                        //Verifica si existe la carpeta de imagenes y si no la crea
                        val arthPathDir = File(musicDir, "arthPath");
                        if(!arthPathDir.exists()){
                            arthPathDir.mkdirs(); //Crea la carpeta
                        }

                        //Si el titulo del archivo es diferente de null
                        if(audioTitle != null){
                            //Buscamos la imagen que inicialmente se guarda en MusicDir
                            val artistImage = File(musicDir, "${audioTitle}.jpg");
                            //Si existe
                            if(artistImage.exists()){
                                //declaramos la nueva ruta
                                val newArtPath = File(arthPathDir, "${audioTitle}.jpg");
                                artistImage.renameTo(newArtPath); //Movemos a la nueva ruta
                            }
                        }



                    }


                    updateNotification("Descarga completada: $title", 100);
                    downloadListener?.invoke(
                        DownloadProgress(
                            isCompleted = true,
                            error = null,
                            downloadedPath = musicDir.absolutePath
                        )
                    );
                    Log.d("DownloadService", "Download completed: $title")
                } else {
                    Log.e("DownloadService", "Exit code: ${result.exitCode}")
                    Log.e("DownloadService", "Error output: ${result.out}")
                    throw Exception("YoutubeDL failed with exit code ${result.exitCode}")
                }



            }catch (e: Exception){
                Log.e("DownloadService", "Download error", e)
                updateNotification("Error en descarga", 0)
                // Notifica el error al ViewModel
                downloadListener?.invoke(
                    DownloadProgress(
                        isCompleted = false,
                        error = e.message ?: "Unknown error"
                    )
                )
            }

        }
    }

    private fun getMusicDirectory(): File{
        val dir = File(
            getExternalFilesDir(null),
            "Music"
        )

        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir;
    }


    private fun updateNotification(title: String, progress: Int){
        //Construye la notificacion
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OrbiMusic - Descargas") // Título fijo
            .setContentText(title) // Texto variable (título de la canción, estado)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Icono
            .setProgress(100, progress, false) // Barra de progreso (0-100)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Baja prioridad
            .build()

        // Convierte el servicio en "foreground service" - aparece notificación permanente
        // Esto previene que el sistema lo cierre por falta de memoria
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    };


    // Retorna el binder para que otras apps se conecten a este servicio
    override fun onBind(intent: Intent?): IBinder = binder;

    // Se ejecuta cuando se destruye el servicio
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Cancela todas las coroutines en progreso
    }

    inner class DownloadServiceBinder : Binder() {
        fun getService(): DownloadMusicService = this@DownloadMusicService
    }

}


data class DownloadProgress(
    val isCompleted: Boolean = false,
    val error: String? = null,
    val downloadedPath: String? = null,
    val progress: Int = 0
)