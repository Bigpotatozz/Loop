package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
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



    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        itemsIndexed(songs) {index, song ->
            MediaSongCard(
                modifier = Modifier.clickable(onClick = { mediaViewModel.playSong(song, index) }),
                title = song.title,
                artist = song.artist,
                albumArt = song.albumArtPath,
                duration = song.duration
            )

            Spacer(Modifier.padding(5.dp))


        }
    }
}