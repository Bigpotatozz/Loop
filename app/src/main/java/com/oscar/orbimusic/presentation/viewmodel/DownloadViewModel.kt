package com.oscar.orbimusic.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.data.audio.DownloadMusicService
import com.oscar.orbimusic.data.audio.DownloadMusicServiceConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class DownloadViewModel(private val context: Context): ViewModel() {

    private var downloadService: DownloadMusicService? = null;
    private lateinit var serviceConnection: DownloadMusicServiceConnection;

    private val _downloadProgress = MutableStateFlow(0);
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow();

    private val _isDownloading = MutableStateFlow(false);
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow();

    private val _downloadError = MutableStateFlow<String?>(null);
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow();

    init {
        connectToDownloadService();
    }




    private fun connectToDownloadService() {
        serviceConnection = DownloadMusicServiceConnection { service ->
            downloadService = service
            service.setDownloadListener { progress ->
                _downloadProgress.value = progress.progress
                _isDownloading.value = !progress.isCompleted
                if (progress.error != null) {
                    _downloadError.value = progress.error
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