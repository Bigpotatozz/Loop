package com.oscar.orbimusic.presentation.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

    val stateList = remember { mutableStateListOf(false, false, true) }


    var isExpanded by rememberSaveable{ mutableStateOf(false) };





    AnimatedContent(
        targetState = isExpanded,
        label = "song_expanded",
        transitionSpec = {
            slideInVertically(initialOffsetY = { it }) + fadeIn() togetherWith
            slideOutVertically(targetOffsetY = { it }) + fadeOut()
        }
    ) { expanded ->

        if(!expanded){

            Column(Modifier.fillMaxSize()) {

                Column(Modifier.padding(15.dp),) {

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Button(onClick = {

                            stateList.replaceAll{false}
                            stateList[0] = !stateList[0];
                            Log.i("OSCAR", "${stateList}")
                            mediaViewModel.loadAllSongsByTitle();
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = if(stateList[0]) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            contentColor = if(stateList[0]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )) {
                            Text("Nombre")
                        }

                        Button(onClick = {
                            stateList.replaceAll{false}
                            stateList[1] = !stateList[1];
                            Log.i("OSCAR", "${stateList}")
                            mediaViewModel.loadAllSongsByArtist();

                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(stateList[1]) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                contentColor = if(stateList[1]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )) {
                            Text("Artista")
                        }

                        Button(onClick = {
                            stateList.replaceAll{false}
                            stateList[2] = !stateList[2];
                            Log.i("OSCAR", "${stateList}")
                            mediaViewModel.loadAllSongs();},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(stateList[2]) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                contentColor = if(stateList[2]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )) {
                            Text("Recientes")
                        }
                    }
                }

                LazyColumn(Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 5.dp)
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
                        ).clickable(onClick = {
                            isExpanded = true
                        })){
                        PrevMediaSong(playerState, mediaViewModel)
                    }

                }
            }


        }else{


            Column() {
                ExpandedPlayerScreen(Modifier,
                    "Perfecta live music",
                    "Eksa y Elmar",
                    duration = 40001,
                    isPlaying = false
                );
            }

        }

    }


}