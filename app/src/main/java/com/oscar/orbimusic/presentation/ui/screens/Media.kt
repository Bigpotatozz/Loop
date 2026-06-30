package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel

@Composable
fun MediaScreen(mediaViewModel: MediaViewModel){

    val songs by mediaViewModel.allSongs.collectAsState();

    Column(Modifier.fillMaxSize()) {

        songs.forEach {
            Text("${it.title}")
        }


    }
}