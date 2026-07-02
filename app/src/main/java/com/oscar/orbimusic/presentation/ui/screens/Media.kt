package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
                duration = song.duration
            )

            Spacer(Modifier.padding(7.dp))
        }
    }

        Column() {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

                Button(onClick = {mediaViewModel.playPrevious()}) {
                    Icon(Icons.Filled.FastRewind, contentDescription = "back")
                }

                Button(onClick = {
                    if(playerState.isPlaying){
                        mediaViewModel.togglePlayPause()
                    }else{
                        mediaViewModel.playSong(playerState.currentSong, playerState.currentIndex);
                    }
                }) {

                    if(playerState.isPlaying){
                        Icon(Icons.Filled.Pause, contentDescription = "Pause")
                    }else{
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                    }

                }

                Button(onClick = {mediaViewModel.playNext()}) {
                    Icon(Icons.Filled.FastForward, contentDescription = "back")
                }
            }


            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {


                Text( if(duration < 0) "" else parseTime(currentPositionMedia), fontSize = 12.sp)

                Slider(
                    value = if (duration > 0) currentPositionMedia.toFloat() else 0f,
                    onValueChange = { newPosition ->
                        mediaViewModel.seekTo(newPosition.toLong())
                    },
                    valueRange = 0f..if (duration > 0) duration.toFloat() else 1f,
                    modifier = Modifier
                        .fillMaxWidth().weight(0.6f),
                    thumb = {},
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.tertiary,
                        inactiveTrackColor = Color.Black.copy(alpha = 0.3f) // Un fondo sutil
                    ),
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            modifier = Modifier
                                .height(10.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                inactiveTrackColor = Color.Black
                            )
                        )
                    }
                )
                Text(if(duration < 0) "" else parseTime(duration), fontSize = 12.sp)


            }

        }





    }


}