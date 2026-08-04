package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.R
import com.oscar.orbimusic.domain.model.PlayerState
import com.oscar.orbimusic.presentation.ui.components.PastPrevMediaSong
import com.oscar.orbimusic.presentation.utils.parseTime
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel
import androidx.compose.ui.layout.ContentScale


/*playerState: PlayerState, mediaViewModel: MediaViewModel*/
@Composable
fun ExpandedPlayerScreen(playerState: PlayerState, mediaViewModel: MediaViewModel){

    val currentPositionSong by mediaViewModel.currentPosition.collectAsState();

    Column(modifier = Modifier.fillMaxSize()
        .padding(8.dp)
        .clip(RoundedCornerShape(25.dp))
        .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,) {

        Row(Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "prev",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(25.dp).clickable(onClick = {
                    mediaViewModel.setExpanded(false);
                })
            )

        }



        AsyncImage(
            model = playerState.currentSong?.albumArtPath,
            contentDescription = playerState.currentSong?.title,
            modifier = Modifier.clip(RoundedCornerShape(25.dp)).size(300.dp),
        )

        Spacer(Modifier.padding(15.dp))


        Text(
            playerState.currentSong?.title ?: "SIN TITULO",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 24.sp, color = Color.White),
            )
        Text(
            playerState.currentSong?.artist ?: "SIN ARTISTA",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, color = Color.Gray))


        Row(Modifier) {
            PastPrevMediaSong(
                playerState,
                mediaViewModel,
                playerState.currentSong!!.duration,
                currentPositionSong,
                parseTime = {
                    parseTime(playerState.currentSong.duration)
                }
            )
        }


        Box(Modifier.padding(15.dp)){
            NextSongComponent(mediaViewModel)
        }




    }

}



@Composable

fun NextSongComponent(mediaViewModel: MediaViewModel){

    val nextSong by mediaViewModel.nextSong.collectAsState();



        Row(Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.primary),
            verticalAlignment = Alignment.CenterVertically) {

            Row(Modifier.weight(0.7f).padding(5.dp),
                verticalAlignment = Alignment.CenterVertically){
                AsyncImage(model = nextSong?.albumArtPath,
                    contentDescription = "Imagen de album",
                    Modifier.clip(RoundedCornerShape(15.dp)).size(60.dp),
                    contentScale = ContentScale.Crop,)


                Column(Modifier.padding(start = 12.dp)) {
                        Text(nextSong?.title ?: "Sin titulo",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),)


                    Text(nextSong?.artist ?: "Sin artista",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 10.sp, color = Color.Gray),)
                }
            }

            Icon( imageVector = Icons.Filled.QueuePlayNext,
                contentDescription = "next",
                modifier = Modifier.size(30.dp)
                    .weight(0.3f)
                    .clickable(onClick = {
                    mediaViewModel.playNext()
                }))







        }

    }



