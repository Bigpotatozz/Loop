package com.oscar.orbimusic.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.domain.model.Song
import com.oscar.orbimusic.domain.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaViewModel (private val songRepository: SongRepository): ViewModel() {



    private val _allSongs = MutableStateFlow<List<Song>>(emptyList());
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow();

    init {
        loadAllSongs()
        Log.d("OSCAR","${allSongs}");
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

}