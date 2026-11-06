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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

//Fila para dar de alta una playlist. Comparte alto y padding con MediaSongCard
//para que se lea como parte de la misma lista y no como algo pegado encima.
@Composable
private fun NewPlaylistRow(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Cuadro del mismo ancho que una caratula, para que el texto arranque
        //alineado con el de las canciones de abajo
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = "Nueva playlist",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.2).sp
            )
        )
    }
}

//El texto vive dentro del dialogo: al cerrarse se va con el, y no hay que
//acordarse de limpiarlo desde la pantalla.
@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Nombre") }
            )
        },
        confirmButton = {
            TextButton(
                //Deshabilitado mientras este en blanco: una playlist sin nombre
                //no se podria distinguir en la lista
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()) }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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
    //Un solo Set para toda la pantalla, en vez de una consulta por fila
    val favoriteIds by mediaViewModel.favoriteIds.collectAsState();

    //0 = nombre, 1 = artista, 2 = recientes, 3 = agrupado por playlist
    var selectedFilter by rememberSaveable { mutableIntStateOf(2) }
    val groupedByPlaylist = selectedFilter == 3

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                mediaViewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

    Column(Modifier.fillMaxSize()) {

                //El titulo conserva su padding; las pildoras salen de el para
                //poder scrollear de orilla a orilla
                Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp)) {

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
                }

                Spacer(Modifier.height(18.dp))

                //LazyRow y no Row: con cuatro pildoras ya no caben en pantallas
                //angostas. El margen va en contentPadding y no en el Modifier
                //para que al scrollear las pildoras pasen por debajo del borde
                //en vez de cortarse contra un padding fijo.
                LazyRow(
                    Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterPill("Nombre", selectedFilter == 0) {
                            selectedFilter = 0
                            mediaViewModel.loadAllSongsByTitle()
                        }
                    }
                    item {
                        FilterPill("Artista", selectedFilter == 1) {
                            selectedFilter = 1
                            mediaViewModel.loadAllSongsByArtist()
                        }
                    }
                    item {
                        FilterPill("Recientes", selectedFilter == 2) {
                            selectedFilter = 2
                            mediaViewModel.loadAllSongs()
                        }
                    }
                    item {
                        //No recarga allSongs: cambia la forma de la lista, no su orden
                        FilterPill("Playlists", selectedFilter == 3) {
                            selectedFilter = 3
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (groupedByPlaylist) {

                        item(key = "new-playlist") {
                            NewPlaylistRow(onClick = { showCreateDialog = true })
                        }

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
                                    isPlaying = playerState.currentSong?.path == song.path && playerState.isPlaying,
                                    isFavorite = song.id in favoriteIds,
                                    onToggleFavorite = { mediaViewModel.toggleFavorite(song.id) }
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
                                isPlaying = playerState.currentSong?.path == song.path && playerState.isPlaying,
                                isFavorite = song.id in favoriteIds,
                                onToggleFavorite = { mediaViewModel.toggleFavorite(song.id) }
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
