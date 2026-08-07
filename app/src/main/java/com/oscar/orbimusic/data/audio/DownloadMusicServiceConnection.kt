package com.oscar.orbimusic.data.audio

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
@UnstableApi
class DownloadMusicServiceConnection
    (private val callback: (DownloadMusicService) -> Unit): ServiceConnection {


    @OptIn(UnstableApi::class)
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as DownloadMusicService.DownloadServiceBinder;
        val downloadService = binder.getService();
        callback(downloadService);
    }

    @OptIn(UnstableApi::class)
    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d("DOWNLOADSERVICE", "Servicio de descarga desconectado");
    }

}