package com.oscar.orbimusic.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.data.audio.AudioPlayerService
import com.oscar.orbimusic.data.audio.AudioServiceConnection
import com.oscar.orbimusic.domain.model.PlayerState
import com.oscar.orbimusic.domain.model.Song
import com.oscar.orbimusic.domain.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaViewModel (private val songRepository: SongRepository,
                      private val context: Context): ViewModel() {


    private var audioService: AudioPlayerService? = null;
    private lateinit var serviceConnection: AudioServiceConnection;
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList());
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow();

    init {
        loadAllSongs()
        Log.d("OSCAR","${allSongs}");
        connectToAudioService()
    }


    private fun connectToAudioService() {
        serviceConnection = AudioServiceConnection(
            callback = { service ->
                audioService = service
                service.setPlayerStateListener { isPlaying, index ->
                    _playerState.update { currentState ->
                        currentState.copy(
                            isPlaying = isPlaying,
                            currentIndex = index,
                            currentSong = if (currentState.queue.isNotEmpty() && index < currentState.queue.size) {
                                currentState.queue[index]
                            } else currentState.currentSong
                        )
                    }
                }
            }
        )
        val intent = Intent(context, AudioPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    private fun loadAllSongs(){
        viewModelScope.launch {
            try{
                songRepository.getAllSongs().collect { songs ->
                    _allSongs.value = songs;
                }
            }finally {
                null
            }
        }
    }



    fun playSong(song: Song?, index: Int = 0) {
        _playerState.update { currentState ->
            currentState.copy(
                currentSong = song,
                isPlaying = true,
                currentIndex = index,
                queue = _allSongs.value,
            )
        }

        audioService?.setPlaylist(_allSongs.value, index)
        audioService?.play()
    }

    fun pauseSong() {
        _playerState.update { it.copy(isPlaying = false) }
        audioService?.pause()
    }

    fun togglePlayPause() {
        _playerState.update { currentState ->
            currentState.copy(isPlaying = !currentState.isPlaying)
        }

        if (_playerState.value.isPlaying) {
            audioService?.play()
        } else {
            audioService?.pause()
        }
    }

    fun playNext() {
        val currentState = _playerState.value
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex < currentState.queue.size) {
            playSong(currentState.queue[nextIndex], nextIndex)
        }
    }

    fun playPrevious() {
        val currentState = _playerState.value

        if (currentState.currentPosition > 3000) {
            _playerState.update { it.copy(currentPosition = 0) }
            audioService?.seekTo(0)
        } else {
            val prevIndex = currentState.currentIndex - 1
            if (prevIndex >= 0) {
                playSong(currentState.queue[prevIndex], prevIndex)
            }
        }
    }

    fun seekTo(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
        audioService?.seekTo(position)
    }

    fun updatePosition(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
    }


    override fun onCleared() {
        super.onCleared()
        context.unbindService(serviceConnection)
    }

}