package com.oscar.orbimusic.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.domain.model.PlayerState
import com.oscar.orbimusic.presentation.ui.animations.PlayerArtKey
import com.oscar.orbimusic.presentation.ui.animations.PlayerContainerKey
import com.oscar.orbimusic.presentation.ui.animations.PlayerTitleKey
import com.oscar.orbimusic.presentation.ui.animations.sharedPlayerBounds
import com.oscar.orbimusic.presentation.ui.animations.sharedPlayerElement
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
            .sharedPlayerBounds(PlayerContainerKey)
            .background(MaterialTheme.colorScheme.secondary)
            .heightIn(min = 68.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val artModifier = Modifier
            .size(44.dp)
            .sharedPlayerElement(PlayerArtKey)
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
                modifier = Modifier.sharedPlayerBounds(PlayerTitleKey),
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
