package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oscar.orbimusic.presentation.ui.animations.LocalPlayerAnimatedScope
import com.oscar.orbimusic.presentation.ui.components.MediaSongCard
import com.oscar.orbimusic.presentation.ui.components.PrevMediaSong
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel

//Pildora de filtro: negra cuando esta seleccionada, con borde hairline cuando no
@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(100)

    Box(
        Modifier
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

//Encabezado de cada bloque de la vista agrupada
@Composable
private fun PlaylistHeader(name: String, songCount: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 18.dp, bottom = 6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        )
        Text(
            text = if (songCount == 1) "1 canción" else "$songCount canciones",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(mediaViewModel: MediaViewModel){

    val songs by mediaViewModel.allSongs.collectAsState();
    val playerState by mediaViewModel.playerState.collectAsState();
    val isExpanded by mediaViewModel.isExpanded.collectAsState();
    val playlistGroups by mediaViewModel.playlistGroups.collectAsState();

    //0 = nombre, 1 = artista, 2 = recientes, 3 = agrupado por playlist
    var selectedFilter by rememberSaveable { mutableIntStateOf(2) }
    val groupedByPlaylist = selectedFilter == 3

    Column(Modifier.fillMaxSize()) {

                Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 16.dp)) {

                    Text(
                        text = "Biblioteca",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = if (groupedByPlaylist) {
                            if (playlistGroups.size == 1) "1 lista" else "${playlistGroups.size} listas"
                        } else {
                            if (songs.size == 1) "1 canción" else "${songs.size} canciones"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill("Nombre", selectedFilter == 0) {
                            selectedFilter = 0
                            mediaViewModel.loadAllSongsByTitle()
                        }
                        FilterPill("Artista", selectedFilter == 1) {
                            selectedFilter = 1
                            mediaViewModel.loadAllSongsByArtist()
                        }
                        FilterPill("Recientes", selectedFilter == 2) {
                            selectedFilter = 2
                            mediaViewModel.loadAllSongs()
                        }
                        //No recarga allSongs: cambia la forma de la lista, no su orden
                        FilterPill("Playlists", selectedFilter == 3) {
                            selectedFilter = 3
                        }
                    }
                }

                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (groupedByPlaylist) {
                        playlistGroups.forEach { group ->

                            item(key = "header-${group.id}") {
                                PlaylistHeader(group.name, group.songs.size)
                            }

                            if (group.songs.isEmpty()) {
                                item(key = "empty-${group.id}") {
                                    Text(
                                        text = "Todavía no hay canciones aquí",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            //La key combina grupo y cancion: la misma cancion
                            //puede estar en varias playlists, y dos items con
                            //la misma key truenan el LazyColumn.
                            itemsIndexed(
                                group.songs,
                                key = { _, song -> "${group.id}-${song.id}" }
                            ) { index, song ->
                                MediaSongCard(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        //playSongs y no playSong: la cola es la
                                        //playlist, no la biblioteca completa
                                        .clickable(onClick = {
                                            mediaViewModel.playSongs(group.songs, index)
                                        }),
                                    title = song.title,
                                    artist = song.artist,
                                    albumArt = song.albumArtPath,
                                    duration = song.duration,
                                    isPlaying = playerState.currentSong?.path == song.path && playerState.isPlaying
                                )
                            }
                        }
                    } else {
                        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                            MediaSongCard(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(onClick = { mediaViewModel.playSong(song, index) }),
                                title = song.title,
                                artist = song.artist,
                                albumArt = song.albumArtPath,
                                duration = song.duration,
                                isPlaying = playerState.currentSong?.path == song.path && playerState.isPlaying
                            )
                        }
                    }
                }

        //La barrita se esconde al expandir: este AnimatedVisibility es el que
        AnimatedVisibility(
            visible = playerState.currentSong != null && !isExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = fadeOut()
        ) {
            CompositionLocalProvider(LocalPlayerAnimatedScope provides this) {
                Box(Modifier
                    .padding(5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = {
                        mediaViewModel.setExpanded(true);
                    })){
                    PrevMediaSong(playerState, mediaViewModel)
                }
            }
        }
    }
}
