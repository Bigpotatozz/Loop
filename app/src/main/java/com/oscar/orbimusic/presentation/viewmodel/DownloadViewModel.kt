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
import com.oscar.orbimusic.data.models.SearchSource
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

    //Fuente elegida por el usuario.
    private val _searchSource = MutableStateFlow(SearchSource.YT_MUSIC);
    val searchSource: StateFlow<SearchSource> = _searchSource.asStateFlow();

    //La fuente que quedo vacia y obligo a reintentar en la otra.
    private val _searchNotice = MutableStateFlow<SearchSource?>(null);
    val searchNotice: StateFlow<SearchSource?> = _searchNotice.asStateFlow();

    //Referencia a la busqueda en curso para poder cancelarla cuando llega otra.
    private var searchJob: Job? = null;

    //El ultimo texto buscado.
    private var lastQuery: String = "";

    //Las descargas que vienen de Deezer no se identifican por el id de YouTube
    //sino por una clave de la fila ("deezer:<id>"): el id de YouTube no se
    //conoce hasta que termina la busqueda. El servicio reporta con el id de
    //YouTube, asi que aqui se traduce de vuelta. Concurrent porque el callback
    //llega desde el hilo del servicio.
    private val rowKeyByVideoId = java.util.concurrent.ConcurrentHashMap<String, String>();

    private fun rowKeyFor(videoId: String): String = rowKeyByVideoId[videoId] ?: videoId;

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
                        rowKeyByVideoId.remove(progress.videoId)
                        _downloadingId.value = null
                        _isDownloading.value = false
                    }

                    progress.isCompleted -> {
                        //La palomita se dispara aqui y no cuando el progreso
                        //llegue a 100: yt-dlp se queda en 99 durante FFmpeg y
                        //nunca reporta el 100
                        _completedDownloads.value =
                            _completedDownloads.value + rowKeyFor(progress.videoId)
                        rowKeyByVideoId.remove(progress.videoId)
                        _downloadingId.value = null
                        _isDownloading.value = false

                        viewModelScope.launch {
                            AppModule.scanMusicFiles(context)
                        }
                    }

                    else -> {
                        _downloadingId.value = rowKeyFor(progress.videoId)
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

    //Lanza la busqueda en la fuente seleccionada. El servicio devuelve el
    //SearchOutcome, que ademas de los resultados dice si hubo cambio de fuente.
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

        lastQuery = query;

        searchJob = viewModelScope.launch {
            _isSearching.value = true;
            //El aviso viejo se limpia al arrancar y no al terminar, para que no
            //se quede un banner de la busqueda anterior encima de los resultados nuevos
            _searchNotice.value = null;
            val outcome = service.searchOnYt(query, _searchSource.value);
            _searchResults.value = outcome.results;
            _searchNotice.value = outcome.fallbackFrom;
            _isSearching.value = false;
        }
    }

    //Cambia de fuente
    fun toggleSearchSource(){
        _searchSource.value = _searchSource.value.other();

        if(lastQuery.isNotBlank()){
            //search() ya cancela la busqueda anterior en su primera linea
            search(lastQuery);
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
        _searchNotice.value = null;
        lastQuery = "";
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

    //Clave de fila para una cancion de Deezer. La pantalla la usa para comparar
    //contra downloadingId y completedDownloads.
    fun deezerRowKey(trackId: Long): String = "deezer:$trackId";

    //Descarga una cancion de Deezer: primero la busca en YouTube Music por
    //"artista titulo" y baja el primer resultado. Si YT Music viene vacio,
    //searchOnYt ya cae a YouTube normal y se usa ese resultado.
    fun downloadDeezerTrack(trackId: Long, artist: String, title: String){
        val service = downloadService ?: return;
        if (_isDownloading.value) return;

        val rowKey = deezerRowKey(trackId);

        //Spinner desde ya: la busqueda tarda unos segundos antes de que el
        //servicio reporte cualquier progreso
        _downloadingId.value = rowKey;
        _downloadProgress.value = -1;
        _isDownloading.value = true;

        viewModelScope.launch {
            val outcome = service.searchOnYt("$artist $title", SearchSource.YT_MUSIC);
            val match = outcome.results.firstOrNull();

            if (match == null) {
                _downloadError.value = "No se encontró \"$title\" en YouTube";
                _downloadingId.value = null;
                _isDownloading.value = false;
                return@launch;
            }

            //fallbackFrom != null = el resultado viene de YouTube normal, se usa
            //su url tal cual. Si no, se arma la de YouTube Music con el id.
            val url = if (outcome.fallbackFrom == null) {
                "https://music.youtube.com/watch?v=${match.id}"
            } else {
                match.url
            };

            Log.d("DOWNLOADVIEWMODEL", "Deezer $trackId -> ${match.id} ($url)");
            rowKeyByVideoId[match.id] = rowKey;
            service.downloadFromYt(match.id, url, title);
        }
    }




}