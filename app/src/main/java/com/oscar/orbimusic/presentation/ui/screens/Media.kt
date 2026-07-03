package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oscar.orbimusic.R
import com.oscar.orbimusic.presentation.ui.components.MediaSongCard
import com.oscar.orbimusic.presentation.ui.components.PrevMediaSong
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(mediaViewModel: MediaViewModel){

    val songs by mediaViewModel.allSongs.collectAsState();
    val playerState by mediaViewModel.playerState.collectAsState();
    val duration by mediaViewModel.duration.collectAsState();
    val currentPositionMedia by mediaViewModel.currentPosition.collectAsState();


    fun parseTime(duration: Long): String {
        if (duration < 0) return "0:00"
        val durationInSeconds = duration / 1000
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60

        return String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    val durationInSeconds = duration / 1000
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60


    Column() {
        LazyColumn(Modifier
            .padding(16.dp)
            .weight(0.8f)) {
        itemsIndexed(songs) {index, song ->
            MediaSongCard(
                modifier = Modifier.clickable(onClick = { mediaViewModel.playSong(song, index) }),
                title = song.title,
                artist = song.artist,
                albumArt = song.albumArtPath,
                duration = song.duration,
                isPlaying = if(playerState.currentSong?.title == song.title && playerState.isPlaying) true else false
            )

            Spacer(Modifier.padding(7.dp))
        }
    }







            AnimatedVisibility(
                visible = playerState.currentSong != null,
                // Efecto de entrada: Aparece desde abajo (300ms) con fundido
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                // Efecto de salida: Se desliza hacia abajo con fundido
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Box(Modifier
                    .padding(5.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        )
                    )){
                    PrevMediaSong(playerState, mediaViewModel)
            }

            }







    }


}