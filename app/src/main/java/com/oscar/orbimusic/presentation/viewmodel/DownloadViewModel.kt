package com.oscar.orbimusic.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.data.audio.DownloadMusicService
import com.oscar.orbimusic.data.audio.DownloadMusicServiceConnection
import com.oscar.orbimusic.data.models.YtSearchResult
import com.oscar.orbimusic.di.AppModule
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@UnstableApi
class DownloadViewModel(private val context: Context): ViewModel() {

    // Service para descargar la musica
    private var downloadService: DownloadMusicService? = null;
    //Conexion del servicio para descargar la muscia
    private lateinit var serviceConnection: DownloadMusicServiceConnection;

    //Flowstate del progreso de la descarga (20...,30...). Vale -1 mientras
    //yt-dlp todavia no reporta nada, que la fila pinta como indeterminado.
    private val _downloadProgress = MutableStateFlow(-1);
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow();

    //Que cancion se esta descargando. Sin esto el progreso no se puede mapear
    //a su fila en la lista de resultados.
    private val _downloadingId = MutableStateFlow<String?>(null);
    val downloadingId: StateFlow<String?> = _downloadingId.asStateFlow();

    //Ids ya descargados, para pintarles la palomita
    private val _completedDownloads = MutableStateFlow<Set<String>>(emptySet());
    val completedDownloads: StateFlow<Set<String>> = _completedDownloads.asStateFlow();

    //Variable que indica si se esta descargando algo
    private val _isDownloading = MutableStateFlow(false);
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow();

    //Variable en caso de error al descargar
    private val _downloadError = MutableStateFlow<String?>(null);
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow();

    //Resultados de la ultima busqueda en YouTube
    private val _searchResults = MutableStateFlow<List<YtSearchResult>>(emptyList());
    val searchResults: StateFlow<List<YtSearchResult>> = _searchResults.asStateFlow();

    //Indica si hay una busqueda en curso
    private val _isSearching = MutableStateFlow(false);
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow();

    //Referencia a la busqueda en curso para poder cancelarla cuando llega otra.
    private var searchJob: Job? = null;

    //Cuando se construye la opantalla se conecta automaticamente al servicio de descargas
    init {
        connectToDownloadService();
    }



    //Funcion de conexion al servicio de descargas
    private fun connectToDownloadService() {
        serviceConnection = DownloadMusicServiceConnection { service ->
            downloadService = service
            service.setDownloadListener { progress ->
                when {
                    progress.error != null -> {
                        _downloadError.value = progress.error
                        _downloadingId.value = null
                        _isDownloading.value = false
                    }

                    progress.isCompleted -> {
                        //La palomita se dispara aqui y no cuando el progreso
                        //llegue a 100: yt-dlp se queda en 99 durante FFmpeg y
                        //nunca reporta el 100
                        _completedDownloads.value =
                            _completedDownloads.value + progress.videoId
                        _downloadingId.value = null
                        _isDownloading.value = false

                        viewModelScope.launch {
                            AppModule.scanMusicFiles(context)
                        }
                    }

                    else -> {
                        _downloadingId.value = progress.videoId
                        _downloadProgress.value = progress.progress
                        _isDownloading.value = true
                    }
                }
            }
        }

        val intent = Intent(context, DownloadMusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        super.onCleared()
        context.unbindService(serviceConnection)
    }

    //Lanza la busqueda en YouTube. El servicio devuelve la lista directo
    fun search(query: String){
        //Si ya habia una busqueda corriendo, se descarta: gana siempre la ultima
        searchJob?.cancel();

        if(query.isBlank()){
            clearSearch();
            return;
        }

        val service = downloadService;
        if(service == null){
            Log.d("DOWNLOADVIEWMODEL", "Busqueda ignorada: el servicio aun no se enlaza");
            return;
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true;
            val results = service.searchOnYt(query);
            _searchResults.value = results;
            _isSearching.value = false;
        }
    }

    //La pantalla lo llama despues de enseñar el error. Sin esto el StateFlow se
    //queda con el ultimo mensaje y el Snackbar vuelve a salir en cada recomposicion.
    fun clearDownloadError(){
        _downloadError.value = null;
    }

    //Limpia los resultados para que la pantalla vuelva a mostrar los widgets
    fun clearSearch(){
        searchJob?.cancel();
        _searchResults.value = emptyList();
        _isSearching.value = false;
        //Las palomitas viven mientras la lista este en pantalla
        _completedDownloads.value = emptySet();
    }

    fun downloadFromUrl(url: String) {
        if (url.isBlank()) return
        val videoId = Regex("""[?&]v=([a-zA-Z0-9_-]{11})""")
            .find(url)?.groupValues?.get(1)
            ?: Regex("""youtu\.be/([a-zA-Z0-9_-]{11})""")
                .find(url)?.groupValues?.get(1)
            ?: url
        DownloadSong(videoId, url, "URL")
    }

    fun DownloadSong(videoId: String, url: String, title: String){
        //Antes era downloadService!!, que tiraba NPE si se tocaba un boton
        //antes de que el servicio terminara de enlazarse
        val service = downloadService ?: return;

        //Se siembra el estado aqui y no se espera al callback: el servicio
        //puede tardar en reportar, y mientras tanto la fila ya debe mostrar
        //el spinner y los demas botones quedar deshabilitados
        _downloadingId.value = videoId;
        _downloadProgress.value = -1;
        _isDownloading.value = true;

        service.downloadFromYt(videoId, url, title);
    }




}