package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.domain.model.PlayerState
import com.oscar.orbimusic.presentation.ui.animations.PlayerArtKey
import com.oscar.orbimusic.presentation.ui.animations.PlayerContainerKey
import com.oscar.orbimusic.presentation.ui.animations.PlayerTitleKey
import com.oscar.orbimusic.presentation.ui.components.AlbumArtPlaceholder
import com.oscar.orbimusic.presentation.ui.theme.PlayerInk
import com.oscar.orbimusic.presentation.ui.theme.PlayerSurface
import com.oscar.orbimusic.presentation.ui.animations.sharedPlayerBounds
import com.oscar.orbimusic.presentation.ui.animations.sharedPlayerElement
import com.oscar.orbimusic.presentation.utils.parseTime
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel


//Boton circular con area de toque accesible. El del mini-player esta fijado a 44/24dp
//y vive en otro archivo, asi que aqui lleva sus propias medidas.
@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    contentDescription: String,
    iconSize: Dp,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = tint
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayerScreen(playerState: PlayerState, mediaViewModel: MediaViewModel){

    val currentPosition by mediaViewModel.currentPosition.collectAsState()
    val shuffle by mediaViewModel.shuffle.collectAsState()
    val repeat by mediaViewModel.repeat.collectAsState()
    val nextSong by mediaViewModel.nextSong.collectAsState()

    val song = playerState.currentSong
    val duration = song?.duration ?: 0L

    //El expandido no sigue al tema: es oscuro en claro y en oscuro. La caratula
  
    val playerInk = PlayerInk
    val background = PlayerSurface

    Column(
        Modifier
            .fillMaxSize()
            .sharedPlayerBounds(PlayerContainerKey, corner = 0.dp, cornerOther = 12.dp)
            .background(background)
    ) {

        //Caratula a sangre: sin padding, pega con el borde superior de la pantalla.
        //La cabecera flota encima y el degradado la funde con el fondo.
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            //Los degradados se pintan DENTRO del nodo de la imagen, para evitar
            // que se pinte y despues de rato se degrade
            val artModifier = Modifier
                .fillMaxSize()
                .sharedPlayerElement(PlayerArtKey)
                .drawWithContent {
                    drawContent()

                    //Velo superior, para que el chevron se lea sobre caratulas claras
                    drawRect(
                        Brush.verticalGradient(
                            colors = listOf(background.copy(alpha = 0.55f), Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.18f
                        )
                    )

                    //Funde el borde inferior con el fondo en vez de cortarlo en seco
                    drawRect(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, background),
                            startY = size.height * 0.6f,
                            endY = size.height
                        )
                    )
                }

            if (song?.albumArtPath == null) {
                AlbumArtPlaceholder(
                    title = song?.title ?: "?",
                    modifier = artModifier,
                    background = playerInk.copy(alpha = 0.12f),
                    contentColor = playerInk.copy(alpha = 0.7f),
                    fontSize = 140.sp
                )
            } else {
                AsyncImage(
                    model = song.albumArtPath,
                    contentDescription = song.title,
                    modifier = artModifier,
                    contentScale = ContentScale.Crop
                )
            }

            //Cabecera flotando sobre la imagen
            Row(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerIconButton(
                    icon = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Cerrar",
                    iconSize = 26.dp,
                    tint = playerInk,
                    onClick = { mediaViewModel.setExpanded(false) }
                )

                PlayerIconButton(
                    icon = Icons.Rounded.FavoriteBorder,
                    contentDescription = "Añadir a favoritos",
                    iconSize = 22.dp,
                    tint = playerInk,
                    // TODO: Agregar a favoritos
                    onClick = { }
                )
            }
        }

        //Titulo y artista
        Text(
            text = song?.title ?: "Sin título",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .sharedPlayerBounds(PlayerTitleKey)
                .basicMarquee(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp
            ),
            color = playerInk,
            maxLines = 1
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = song?.artist ?: "Sin artista",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = playerInk.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(24.dp))

        //Bloque inferior: lleva su propio margen lateral y se aparta de la barra
        //de gestos. La imagen de arriba no se aparta de nada, por eso los insets
        //ya no estan en la Column raiz.
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {

        //Progreso
        Slider(
            value = if (duration > 0) currentPosition.toFloat() else 0f,
            onValueChange = { mediaViewModel.seekTo(it.toLong()) },
            valueRange = 0f..if (duration > 0) duration.toFloat() else 1f,
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                Box(
                    Modifier
                        .size(width = 4.dp, height = 20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(playerInk)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier
                        .height(8.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    colors = SliderDefaults.colors(
                        activeTrackColor = playerInk,
                        inactiveTrackColor = playerInk.copy(alpha = 0.25f)
                    )
                )
            },
            colors = SliderDefaults.colors(
                activeTrackColor = playerInk,
                inactiveTrackColor = playerInk.copy(alpha = 0.25f)
            )
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = parseTime(currentPosition),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = playerInk.copy(alpha = 0.6f)
            )
            Text(
                text = parseTime(duration),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = playerInk.copy(alpha = 0.6f)
            )
        }

        Spacer(Modifier.height(20.dp))

        //Controles
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerIconButton(
                icon = Icons.Rounded.Shuffle,
                contentDescription = if (shuffle) "Desactivar aleatorio" else "Activar aleatorio",
                iconSize = 22.dp,
                tint = if (shuffle) playerInk else playerInk.copy(alpha = 0.4f),
                onClick = { mediaViewModel.setShuffle(!shuffle) }
            )

            PlayerIconButton(
                icon = Icons.Rounded.SkipPrevious,
                contentDescription = "Canción anterior",
                iconSize = 32.dp,
                tint = playerInk,
                onClick = { mediaViewModel.playPrevious() }
            )

            //Unico elemento relleno: el peso visual de la pantalla
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(playerInk)
                    .clickable(onClick = { mediaViewModel.togglePlayPause() }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "Pausar" else "Reproducir",
                    modifier = Modifier.size(28.dp),
                    tint = background
                )
            }

            PlayerIconButton(
                icon = Icons.Rounded.SkipNext,
                contentDescription = "Siguiente canción",
                iconSize = 32.dp,
                tint = playerInk,
                onClick = { mediaViewModel.playNext() }
            )

            PlayerIconButton(
                icon = Icons.Rounded.Repeat,
                contentDescription = if (repeat) "Desactivar repetición" else "Repetir canción",
                iconSize = 22.dp,
                tint = if (repeat) playerInk else playerInk.copy(alpha = 0.4f),
                onClick = { mediaViewModel.setRepeat(!repeat) }
            )
        }

        Spacer(Modifier.height(12.dp))

        //Siguiente en la cola: una linea, no una tarjeta
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = nextSong != null) { mediaViewModel.playNextNoShuffle() }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sigue",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = playerInk.copy(alpha = 0.5f)
            )
            Text(
                text = "  ·  ",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = playerInk.copy(alpha = 0.3f)
            )
            Text(
                text = nextSong?.title ?: "Fin de la cola",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = if (nextSong != null) playerInk else playerInk.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

            Spacer(Modifier.height(8.dp))
        }
    }
}
