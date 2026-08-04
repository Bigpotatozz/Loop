package com.oscar.orbimusic.presentation.ui.components

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.R
import com.oscar.orbimusic.domain.model.PlayerState
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel


@Composable
fun PrevMediaSong(playerState: PlayerState, mediaViewModel: MediaViewModel){

            Row(Modifier.background(MaterialTheme.colorScheme.secondary).padding(15.dp),verticalAlignment = Alignment.CenterVertically) {

                AsyncImage(
                    model = playerState.currentSong!!.albumArtPath,
                    contentDescription = playerState.currentSong.title,
                    modifier = if(playerState.isPlaying) Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(10.dp))
                    else Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
                )

                Column(Modifier.padding(start = 12.dp).weight(0.8f)) {
                    Text(playerState.currentSong!!.title,
                        style = TextStyle( fontWeight = FontWeight.Bold),
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary)
                    Text(playerState.currentSong!!.artist,
                        fontSize = 15.sp,
                        color = Color.Gray) }
                Row() {
                    Icon( imageVector = Icons.Filled.FastRewind,
                        contentDescription = "prev",
                        modifier = Modifier.size(30.dp).clickable(onClick = {
                            mediaViewModel.playPrevious();
                        }),
                        tint = MaterialTheme.colorScheme.primary
                        )

                    Icon( imageVector = if(playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "play/pause",
                        modifier = Modifier.size(30.dp).clickable(onClick = {
                            if(playerState.isPlaying){
                                mediaViewModel.pauseSong();

                            }else{
                                mediaViewModel.playSong(playerState.currentSong);
                            }
                        }),
                        tint = MaterialTheme.colorScheme.primary)

                    Icon( imageVector = Icons.Filled.FastForward,
                        contentDescription = "next",
                        modifier = Modifier.size(30.dp).clickable(onClick = {
                            mediaViewModel.playNext()
                        }),
                        tint = MaterialTheme.colorScheme.primary)

                }








        }






}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastPrevMediaSong(playerState: PlayerState,
                      mediaViewModel: MediaViewModel,
                      duration: Long,
                      currentPositionMedia: Long,
                      parseTime: (time: Long) -> String){


    val shuffle by mediaViewModel.shuffle.collectAsState();
    val repeat by mediaViewModel.repeat.collectAsState();

    var shuffleLocal = false;
    var repeatLocal = false;


    Column () {


        Row(Modifier
            .fillMaxWidth()
            .padding(10.dp), verticalAlignment = Alignment.CenterVertically) {

            Text(parseTime(currentPositionMedia), fontSize = 11.sp, color = Color.Gray)



            Slider(
                value = if (duration > 0) currentPositionMedia.toFloat() else 0f,
                onValueChange = { newPosition ->
                    mediaViewModel.seekTo(newPosition.toLong())
                },
                valueRange = 0f..if (duration > 0) duration.toFloat() else 1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
                thumb = {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.tertiary,
                    inactiveTrackColor = Color.Gray
                ),
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier
                            .height(10.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.tertiary,
                            inactiveTrackColor = Color.Gray
                        )
                    )
                }
            )



            Text(parseTime(duration), fontSize = 11.sp, color = Color.Gray)


        }
        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

            Icon( imageVector = Icons.Filled.Shuffle,
                contentDescription = "shuffle",
                modifier = Modifier.size(25.dp).clickable(onClick = {

                    shuffleLocal = !shuffle;
                    mediaViewModel.setShuffle(shuffleLocal)
                }),
                tint = if(!shuffle) Color.Gray else MaterialTheme.colorScheme.primary)

            Spacer(Modifier.padding(5.dp))


            Icon( imageVector = Icons.Filled.FastRewind,
                contentDescription = "prev",
                modifier = Modifier.size(30.dp).clickable(onClick = {
                    mediaViewModel.playPrevious();
                }),
                tint = MaterialTheme.colorScheme.primary
            )


            Spacer(Modifier.padding(10.dp))
            Button(
                onClick = {
                    if (playerState.isPlaying) {
                        mediaViewModel.togglePlayPause()
                    } else {
                        mediaViewModel.playSong(playerState.currentSong, playerState.currentIndex);
                    }
                },
            ) {

                if(playerState.isPlaying){
                    Icon(Icons.Filled.Pause, contentDescription = "Pause")
                }else{
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                }

            }
            Spacer(Modifier.padding(10.dp))

            Icon( imageVector = Icons.Filled.FastForward,
                contentDescription = "next",
                modifier = Modifier.size(30.dp).clickable(onClick = {
                    mediaViewModel.playNext()
                }),
                tint = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.padding(5.dp))

            Icon( imageVector = Icons.Filled.Repeat,
                contentDescription = "repeat",
                modifier = Modifier.size(25.dp).clickable(onClick = {

                    repeatLocal = !repeat;
                    mediaViewModel.setRepeat(repeatLocal);
                }),
                tint = if(!repeat) Color.Gray else MaterialTheme.colorScheme.primary)





        }




    }

}