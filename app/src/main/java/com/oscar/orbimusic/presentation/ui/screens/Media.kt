package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oscar.orbimusic.presentation.ui.components.MediaSongCard
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel

@Composable
fun MediaScreen(mediaViewModel: MediaViewModel){

    val songs by mediaViewModel.allSongs.collectAsState();

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        songs.forEach {
            MediaSongCard(it.title, it.artist, it.albumArtPath, it.duration)
            Spacer(Modifier.padding(5.dp))
        }


    }
}