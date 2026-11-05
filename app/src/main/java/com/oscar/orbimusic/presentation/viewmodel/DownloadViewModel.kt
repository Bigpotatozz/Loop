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

    //Flowstate del progreso de la descarga (20...,30...)
    private val _downloadProgress = MutableStateFlow(0);
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow();

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
                _downloadProgress.value = progress.progress
                _isDownloading.value = !progress.isCompleted
                if (progress.error != null) {
                    _downloadError.value = progress.error
                }

                if (progress.isCompleted) {
                    viewModelScope.launch {
                        AppModule.scanMusicFiles(context)
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

    //Limpia los resultados para que la pantalla vuelva a mostrar los widgets
    fun clearSearch(){
        searchJob?.cancel();
        _searchResults.value = emptyList();
        _isSearching.value = false;
    }

    fun DownloadSong(url: String, title: String){
        downloadService!!.downloadFromYt(url, title);
    }




}