package com.oscar.orbimusic.data.audio

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log


interface AudioServiceCallback{
    fun onServiceConnected(service: AudioPlayerService);
}

class AudioServiceConnection (
    private val callback: (AudioPlayerService) -> Unit
): ServiceConnection{

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

        val binder = service as AudioPlayerService.AudioPlayerBinder

        val audioService = binder.getService()

        callback(audioService)

    }


    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d("AudioServiceConnection", "Desconectado del Service")
    }

}