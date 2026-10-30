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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.R
import com.oscar.orbimusic.domain.model.PlayerState
import com.oscar.orbimusic.presentation.ui.theme.OrbiMusicTheme
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel


@Composable
fun PrevMediaSong(playerState: PlayerState, mediaViewModel: MediaViewModel){

    val song = playerState.currentSong ?: return

    MiniPlayerContent(
        title = song.title,
        artist = song.artist,
        albumArt = song.albumArtPath,
        isPlaying = playerState.isPlaying,
        onPrevious = { mediaViewModel.playPrevious() },
        onPlayPause = { mediaViewModel.togglePlayPause() },
        onNext = { mediaViewModel.playNext() }
    )
}


//Boton secundario: icono plano dentro de un area de toque circular de 44dp
@Composable
private fun MiniPlayerIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}


@Composable
private fun MiniPlayerContent(
    title: String,
    artist: String,
    albumArt: String?,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
){
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .heightIn(min = 68.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val artModifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))

        if (albumArt == null) {
            AlbumArtPlaceholder(
                title = title,
                modifier = artModifier,
                background = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                fontSize = 17.sp
            )
        } else {
            AsyncImage(
                model = albumArt,
                contentDescription = title,
                modifier = artModifier,
                contentScale = ContentScale.Crop
            )
        }

        Column(
            Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                title,
                style = TextStyle(fontWeight = FontWeight.SemiBold),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                artist,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniPlayerIconButton(Icons.Filled.SkipPrevious, "Canción anterior", onPrevious)

            //Unico elemento relleno de la barra: ahi se concentra el peso visual
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            MiniPlayerIconButton(Icons.Filled.SkipNext, "Siguiente canción", onNext)
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun MiniPlayerContentPreview(){
    OrbiMusicTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.clip(RoundedCornerShape(20.dp))) {
                MiniPlayerContent(
                    title = "Smells Like Teen Spirit",
                    artist = "Nirvana",
                    albumArt = null,
                    isPlaying = true,
                    onPrevious = {}, onPlayPause = {}, onNext = {}
                )
            }
            Box(Modifier.clip(RoundedCornerShape(20.dp))) {
                MiniPlayerContent(
                    title = "Something In The Way",
                    artist = "Nirvana",
                    albumArt = null,
                    isPlaying = false,
                    onPrevious = {}, onPlayPause = {}, onNext = {}
                )
            }
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