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
import com.oscar.orbimusic.di.AppModule
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

    fun DownloadSong(url: String, title: String){
        downloadService!!.downloadFromYt(url, title);
    }




}