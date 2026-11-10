package com.oscar.orbimusic.data.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.R
import com.oscar.orbimusic.data.models.DownloadProgress
import com.oscar.orbimusic.data.models.SearchOutcome
import com.oscar.orbimusic.data.models.SearchSource
import com.oscar.orbimusic.data.models.YtSearchResult
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
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

    //Serializa las descargas: una a la vez. Si se tocan varios botones, las
    //demas esperan su turno en vez de correr en paralelo.
    private val downloadMutex = Mutex()

    //Para no repetir startForeground en cada tick: la primera notificacion
    //levanta el foreground, las siguientes solo la actualizan.
    private var isForeground = false

    companion object {
        //Canal para la notificacion
        private const val CHANNEL_ID = "music_download_channel"
        // ID de la notificación
        private const val NOTIFICATION_ID = 2
        //Cuantos resultados se le piden a YouTube Music por busqueda
        private const val SEARCH_RESULTS = 10
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

                //Solo actualiza si han pasado mas de 24h desde la ultima vez,
                //evita el delay en cada apertura de pantalla
                val prefs = applicationContext.getSharedPreferences("ytdl", MODE_PRIVATE)
                val lastUpdate = prefs.getLong("last_update", 0L)
                val oneDayMs = 24 * 60 * 60 * 1000L
                if (System.currentTimeMillis() - lastUpdate > oneDayMs) {
                    try {
                        YoutubeDL.getInstance().updateYoutubeDL(this@DownloadMusicService)
                        prefs.edit().putLong("last_update", System.currentTimeMillis()).apply()
                        Log.d("DownloadService", "YouTubeDL actualizado")
                    } catch (e: Exception) {
                        //Que falle la actualizacion no debe dejar la pantalla
                        //muerta: el binario que ya estaba sigue sirviendo. No se
                        //escribe el timestamp, asi se reintenta en la proxima apertura.
                        Log.w("DownloadService", "No se pudo actualizar YouTubeDL, se sigue con el binario actual", e)
                    }
                } else {
                    Log.d("DownloadService", "YouTubeDL al dia, se omite actualizacion")
                }

                isInitialized.complete(true)
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


    //Busca en la fuente que pidio el usuario. Si eligio YT Music y vuelve vacio,
    //reintenta en YouTube y lo reporta en fallbackFrom para que la pantalla avise.

    suspend fun searchOnYt(query: String, source: SearchSource): SearchOutcome =
        withContext(Dispatchers.IO) {

            if (query.isBlank()) return@withContext SearchOutcome(emptyList())

            if (!isInitialized.await()) {
                Log.e("DownloadService", "YouTubeDL no inicializado")
                return@withContext SearchOutcome(emptyList())
            }

            val results = executeSearch(buildSearchRequest(source, query), source.label)

            if (results.isNotEmpty() || source != SearchSource.YT_MUSIC) {
                return@withContext SearchOutcome(results)
            }

            val fallback = source.other()
            Log.d("DownloadService", "Sin resultados en ${source.label}, reintentando en ${fallback.label}")
            val fallbackResults = executeSearch(buildSearchRequest(fallback, query), fallback.label)

            //Si el fallback tampoco trajo nada no hay nada que avisar: para la
            //pantalla es una busqueda vacia normal, no un cambio de fuente
            SearchOutcome(
                results = fallbackResults,
                fallbackFrom = if (fallbackResults.isNotEmpty()) source else null
            )
        }

    
    private fun buildSearchRequest(source: SearchSource, query: String): YoutubeDLRequest {
        val request = when (source) {
            SearchSource.YT_MUSIC -> {
                val encoded = URLEncoder.encode(query, "UTF-8")
                YoutubeDLRequest("https://music.youtube.com/search?q=$encoded#songs").apply {
                    addOption("--playlist-end", SEARCH_RESULTS.toString())
                }
            }

            SearchSource.YOUTUBE -> YoutubeDLRequest("ytsearch${SEARCH_RESULTS}:$query")
        }

        return request.apply {
            addOption("--flat-playlist")
            addOption("--dump-json")
            addOption("--no-warnings")
        }
    }

    private fun executeSearch(request: YoutubeDLRequest, source: String): List<YtSearchResult> {
        return try {
            val result = YoutubeDL.getInstance().execute(request)

            if (result.exitCode != 0) {
                Log.w("DownloadService", "$source: búsqueda fallida (${result.exitCode})")
                return emptyList()
            }

            val results = result.out
                .lineSequence()
                .map { it.trim() }
                .filter { it.startsWith("{") }
                .mapNotNull { parseSearchResult(it) }
                .distinctBy { it.id }
                .toList()

            Log.d("DownloadService", "$source: ${results.size} resultados")
            results

        } catch (e: Exception) {
            Log.w("DownloadService", "$source: error en búsqueda", e)
            emptyList()
        }
    }


    //Convierte una linea de --dump-json en YtSearchResult. Devuelve null si la
    //linea viene incompleta, para que un resultado roto no tumbe los otros 19.
    private fun parseSearchResult(line: String): YtSearchResult? {
        return try {
            val json = JSONObject(line)
            val id = json.optString("id").takeIf { it.isNotBlank() } ?: return null

            YtSearchResult(
                id = id,
                title = json.optString("title").ifBlank { "Sin titulo" },
                //YouTube Music no manda ninguno de estos dos: yt-dlp solo saca
                //videoId y titulo de sus resultados.
                channel = json.optString("channel")
                    .ifBlank { json.optString("uploader") }
                    .ifBlank { "Desconocido" },
                durationSeconds = json.optDouble("duration", 0.0)
                    .takeIf { !it.isNaN() }?.toLong() ?: 0L,
                //mqdefault y no hqdefault: hqdefault es 480x360, o sea 4:3, y
                //mete el fotograma 16:9 con barras negras arriba y abajo. Al
                //recortarlo a cuadrado las barras se quedan dentro y parecen un
                //borde. mqdefault es 320x180, 16:9 de verdad y sin relleno.
                //Las dos existen siempre para cualquier video, a diferencia de
                //maxresdefault, que falla en muchos.
                thumbnailUrl = "https://i.ytimg.com/vi/$id/mqdefault.jpg",
                url = json.optString("webpage_url")
                    .ifBlank { json.optString("url") }
                    .takeIf { it.startsWith("http") }
                    ?: "https://www.youtube.com/watch?v=$id"
            )
        } catch (e: Exception) {
            Log.w("DownloadService", "Linea de busqueda ilegible, se omite", e)
            null
        }
    }


    fun downloadFromYt(videoId: String, youtubeUrl: String, title: String){
        scope.launch {

            //Una descarga a la vez.
            downloadMutex.withLock {
                runDownload(videoId, youtubeUrl, title)
            }

        }
    }


    private suspend fun runDownload(videoId: String, youtubeUrl: String, title: String) {
        try {
            // Espera a que la inicialización termine antes de continuar para evitar IllegalStateException
            if (!isInitialized.await()) {
                throw Exception("YouTubeDL failed to initialize")
            }

            //Se avisa de inmediato con progreso -1 para que la fila pinte el
            //spinner indeterminado: yt-dlp todavia no reporta nada util
            notifyProgress(videoId, title, -1)

            val musicDir = getMusicDirectory();
            Log.d("DownloadService", "Descargando a: ${musicDir.absolutePath}");

            //El callback se dispara por cada linea de salida de yt-dlp. Sin el
            //filtro por entero serian cientos de notificaciones por descarga.
            var lastReported = -1
            val onProgress: (Float, Long, String) -> Unit = { progress, _, _ ->
                val percent = progress.toInt()
                if (percent != lastReported && percent >= 0) {
                    lastReported = percent
                    notifyProgress(videoId, title, percent)
                }
            }

            //Intento 1: URL original (YouTube Music).
            //Intento 2: si falla, YouTube normal con el mismo video ID.
            val ytUrl = "https://www.youtube.com/watch?v=$videoId"
            val result = try {
                YoutubeDL.getInstance()
                    .execute(buildRequest(youtubeUrl, musicDir), null, false, onProgress)
            } catch (e: YoutubeDLException) {
                Log.w("DownloadService", "Falló YT Music, reintentando con YouTube normal", e)
                YoutubeDL.getInstance()
                    .execute(buildRequest(ytUrl, musicDir), null, false, onProgress)
            }

            if (result.exitCode == 0){

                val filesDownloaded = musicDir.listFiles(){file ->
                    file.extension == "m4a"
                }?.sortedByDescending { it.lastModified() }

                if(!filesDownloaded.isNullOrEmpty()){

                    //Verifica si existe la carpeta de album_art y si no la crea
                    val albumArtDir = File(musicDir, "arthPath");
                    if(!albumArtDir.exists()){
                        albumArtDir.mkdirs(); //Crea la carpeta
                    }

                    //Buscar el jpg más reciente descargado (sin depender del nombre)
                    val jpgFiles = musicDir.listFiles { file ->
                        file.extension == "jpg"
                    }?.sortedByDescending { it.lastModified() }

                    if (!jpgFiles.isNullOrEmpty()) {
                        val newestJpg = jpgFiles.first()
                        val newPath = File(albumArtDir, newestJpg.name)
                        newestJpg.renameTo(newPath)
                        Log.d("DownloadService", "JPG movido a album_art: ${newestJpg.name}")

                        cropToCenteredSquare(newPath)
                    }



                }


                //La descarga termino: la notificacion deja de ser permanente y
                //el servicio sale de foreground para que se pueda descartar
                finishNotification("Descarga completada: $title")
                downloadListener?.invoke(
                    DownloadProgress(
                        videoId = videoId,
                        isCompleted = true,
                        error = null,
                        downloadedPath = musicDir.absolutePath,
                        progress = 100
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
            finishNotification("Error al descargar: $title")
            // Notifica el error al ViewModel
            downloadListener?.invoke(
                DownloadProgress(
                    videoId = videoId,
                    isCompleted = false,
                    error = friendlyError(e.message)
                )
            )
        }
    }


    private fun friendlyError(raw: String?): String {
        val message = raw?.trim().orEmpty()
        return when {
            message.isBlank() -> "No se pudo descargar"
            message.contains("confirm your age", ignoreCase = true) ->
                "YouTube pide iniciar sesion para este video"
            message.contains("not a bot", ignoreCase = true) ->
                "YouTube pide verificacion, intentalo en un momento"
            message.contains("Private video", ignoreCase = true) ->
                "El video es privado"
            message.contains("Video unavailable", ignoreCase = true) ->
                "Video no disponible"
            message.contains("Unable to download", ignoreCase = true) ||
            message.contains("Unable to connect", ignoreCase = true) ->
                "Sin conexion con YouTube"
            else -> message.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotBlank() }
                ?.removePrefix("ERROR: ")
                ?.take(140)
                ?: "No se pudo descargar"
        }
    }


    //Actualiza notificacion y ViewModel con el mismo progreso
    private fun notifyProgress(videoId: String, title: String, percent: Int) {
        updateNotification("Descargando: $title", percent.coerceAtLeast(0))
        downloadListener?.invoke(
            DownloadProgress(videoId = videoId, progress = percent)
        )
    }

    //Opciones de la descarga. Extraido aqui para poder repetir la misma peticion
    //en el reintento cambiando unicamente lo de la cache.
    private fun buildRequest(
        youtubeUrl: String,
        musicDir: File,
        clearCache: Boolean = false
    ): YoutubeDLRequest = YoutubeDLRequest(youtubeUrl).apply {
        if (clearCache) addOption("--rm-cache-dir")

        addOption("--extractor-args", "youtube:player_client=android")
        addOption("-f", "bestaudio[ext=m4a]/best")
        addOption("-x")
        addOption("--audio-format", "m4a")
        addOption("--audio-quality", "0")
        addOption("--add-metadata")
        addOption("--embed-metadata")
        addOption("--write-thumbnail") //Obtiene la imagen
        addOption("--convert-thumbnail", "jpg") //Guarda la imagen en jpg
        addOption("-o", "${musicDir.absolutePath}/%(title)s.%(ext)s")
    }


    //Recorta la caratula a cuadrada, recibe el archivo y lo sobreescribe
    private fun cropToCenteredSquare(file: File) {
        try {
            // Decodifica la imagen
            val original = BitmapFactory.decodeFile(file.path) ?: return
            //Si ya es cuadrada simplemente la vuelve a usar y la retorna
            if (original.width == original.height) {
                original.recycle()
                return
            }
            //Saca las medidas originales de la imagen
            val side = minOf(original.width, original.height)

            //Saca el centro y hace el recorte
            val left = (original.width - side) / 2
            val top = (original.height - side) / 2
            val square = Bitmap.createBitmap(original, left, top, side, side)
            // Lo parsea a JPG
            FileOutputStream(file).use { out ->
                square.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            //createBitmap puede devolver la misma instancia si el recorte abarca
            if (square != original) original.recycle()
            square.recycle()

            Log.d("DownloadService", "Carátula recortada a ${side}x$side: ${file.name}")

        } catch (e: Exception) {
            //Un recorte fallido no debe tumbar la descarga: se queda el 16:9
            Log.e("DownloadService", "No se pudo recortar la carátula ${file.name}", e)
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


    private fun buildNotification(text: String, progress: Int, ongoing: Boolean) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Loop - Descargas")
            .setContentText(text) // Texto variable (título de la canción, estado)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Icono
            .setPriority(NotificationCompat.PRIORITY_LOW) // Baja prioridad
            .setOngoing(ongoing)
            .apply {
                //Si todavia no hay porcentaje, barra indeterminada en lugar de 0%
                when {
                    progress >= 0 -> setProgress(100, progress, false)
                    ongoing -> setProgress(0, 0, true)
                }
            }
            .build()


    private fun updateNotification(title: String, progress: Int){
        val notification = buildNotification(title, progress, ongoing = true)

        // Convierte el servicio en "foreground service" - aparece notificación permanente
        // Esto previene que el sistema lo cierre por falta de memoria
        if (!isForeground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            isForeground = true
        } else {
            getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, notification)
        }
    };


    //La descarga acabo, bien o mal: se quita el ongoing y el servicio sale de
    //foreground para que la notificacion se pueda descartar. Antes nadie
    //llamaba a stopForeground y se quedaba pegada para siempre.
    private fun finishNotification(text: String) {
        stopForeground(STOP_FOREGROUND_DETACH)
        isForeground = false
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(text, progress = -1, ongoing = false))
    }


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


