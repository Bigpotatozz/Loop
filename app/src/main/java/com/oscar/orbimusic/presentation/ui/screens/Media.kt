package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.oscar.orbimusic.data.local.database.entities.PlaySource
import com.oscar.orbimusic.domain.models.PlaylistSelection
import com.oscar.orbimusic.domain.models.PlaylistSummary
import com.oscar.orbimusic.domain.models.Song
import com.oscar.orbimusic.presentation.ui.animations.LocalPlayerAnimatedScope
import com.oscar.orbimusic.presentation.ui.animations.LocalPlaylistAnimatedScope
import com.oscar.orbimusic.presentation.ui.animations.playlistCoverKey
import com.oscar.orbimusic.presentation.ui.animations.sharedPlaylistElement
import com.oscar.orbimusic.presentation.ui.theme.FavoriteRed
import com.oscar.orbimusic.presentation.utils.parseTotalTime
import kotlin.random.Random
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
@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(Modifier.padding(24.dp)) {

                Text(
                    text = "Nueva playlist",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "Ponle un nombre para reconocerla después",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(18.dp))

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            text = "Mis rolas",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    //La tecla del teclado tambien crea, para no obligar a
                    //cerrarlo antes de poder tocar el boton
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank()) onConfirm(name.trim())
                    }),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    )

                    Spacer(Modifier.width(8.dp))
                    val enabled = name.isNotBlank()
                    Text(
                        text = "Crear",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .background(
                                if (enabled) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable(enabled = enabled) { onConfirm(name.trim()) }
                            .padding(horizontal = 20.dp, vertical = 9.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistCoverMosaic(
    coverPaths: List<String>,
    fallbackIcon: ImageVector,
    fallbackTint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            //Playlist vacia, o con canciones sin portada
            coverPaths.isEmpty() -> {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = fallbackTint
                )
            }

            coverPaths.size < 4 -> {
                AsyncImage(
                    model = coverPaths.first(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            else -> {
                //Rejilla 2x2 sin separacion: cada celda es un cuarto del cuadro
                Column(Modifier.fillMaxSize()) {
                    for (fila in 0 until 2) {
                        Row(Modifier.weight(1f)) {
                            for (columna in 0 until 2) {
                                AsyncImage(
                                    model = coverPaths[fila * 2 + columna],
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeletePlaylistDialog(
    name: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(Modifier.padding(24.dp)) {

                Text(
                    text = "¿Eliminar \"$name\"?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "La playlist se borra y no se puede recuperar. Tus canciones no se tocan.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    //Rojo y no la tinta de siempre: es la accion destructiva
                    Text(
                        text = "Eliminar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .background(FavoriteRed)
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 20.dp, vertical = 9.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun DeleteSongDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(Modifier.padding(24.dp)) {

                Text(
                    text = "¿Eliminar \"$title\"?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "El archivo se borra del dispositivo y la canción sale de tus favoritos y de todas tus playlists. No se puede recuperar.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Eliminar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .background(FavoriteRed)
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 20.dp, vertical = 9.dp)
                    )
                }
            }
        }
    }
}


//Envuelve una fila para poder quitarla deslizando hacia la izquierda
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRemove(
    onRemove: () -> Unit,
    enabled: Boolean = true,
    label: String = "Quitar",
    dismissOnSwipe: Boolean = true,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                dismissOnSwipe
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = enabled,
        backgroundContent = {
            if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FavoriteRed)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        },
        content = { content() }
    )
}


//Boton de la cabecera del detalle. Misma forma que una FilterPill: relleno
//para la accion principal, con borde para la secundaria.
@Composable
private fun PlaylistActionButton(
    label: String,
    icon: ImageVector,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(100)
    //Deshabilitado se apaga pero conserva la forma, para que no parezca que
    //desaparecio un boton
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        filled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    Row(
        modifier
            .clip(shape)
            .background(
                when {
                    !enabled -> MaterialTheme.colorScheme.surfaceVariant
                    filled -> MaterialTheme.colorScheme.secondary
                    else -> Color.Transparent
                }
            )
            .border(
                width = 1.dp,
                color = if (!filled && enabled) MaterialTheme.colorScheme.outline
                else Color.Transparent,
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}


//Cabecera del detalle. Va como primer item del LazyColumn y no fija arriba,
//para que se vaya con el scroll y la pantalla quede para las canciones.
@Composable
private fun PlaylistDetailHeader(
    summary: PlaylistSummary,
    songs: List<Song>,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    val isFavorites = summary.id == null
    val hasSongs = songs.isNotEmpty()
    //El degradado tiene que fundir con el fondo real de la pantalla, que es
    //primary: en este tema primary es el color de fondo, no un acento
    val background = MaterialTheme.colorScheme.primary

    Column(Modifier.fillMaxWidth()) {

        Box(Modifier.fillMaxWidth()) {

            //aspectRatio(1f) y no una altura fija: ocupa todo el ancho
            //disponible y se mantiene cuadrada en cualquier pantalla, que es
            //lo que el mosaico 2x2 necesita para no descuadrarse.
            //La clave compartida es la misma que la fila del primer nivel.
            PlaylistCoverMosaic(
                coverPaths = summary.coverPaths,
                fallbackIcon = if (isFavorites) Icons.Rounded.Favorite
                else Icons.Rounded.QueueMusic,
                fallbackTint = if (isFavorites) FavoriteRed
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .sharedPlaylistElement(playlistCoverKey(summary.id))
                    //Solo el degradado inferior: funde el borde con el fondo en
                    //vez de cortarlo en seco
                    .drawWithContent {
                        drawContent()

                        drawRect(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, background),
                                startY = size.height * 0.55f,
                                endY = size.height
                            )
                        )
                    }
            )

            //Flotando sobre la imagen, como el chevron del expandido
            Box(
                Modifier
                    .padding(start = 6.dp, top = 6.dp)
                    .size(44.dp)
                    .clip(RoundedCornerShape(100))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Regresar a las playlists",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        //Sin Spacer: el degradado ya dejo aire visual al fundir la imagen
        Column(Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = summary.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = buildString {
                    append(if (songs.size == 1) "1 canción" else "${songs.size} canciones")
                    if (hasSongs) {
                        append(" · ")
                        append(parseTotalTime(songs.sumOf { it.duration }))
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlaylistActionButton(
                    label = "Reproducir",
                    icon = Icons.Rounded.PlayArrow,
                    filled = true,
                    enabled = hasSongs,
                    onClick = onPlay,
                    modifier = Modifier.weight(1f)
                )
                PlaylistActionButton(
                    label = "Aleatorio",
                    icon = Icons.Rounded.Shuffle,
                    filled = false,
                    enabled = hasSongs,
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}


@Composable
private fun PlaylistSummaryRow(
    summary: PlaylistSummary,
    onClick: () -> Unit
) {
    val isFavorites = summary.id == null

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaylistCoverMosaic(
            coverPaths = summary.coverPaths,
            //Favoritos se distingue con su corazon rojo cuando no hay portadas
            fallbackIcon = if (isFavorites) Icons.Rounded.Favorite
            else Icons.Rounded.QueueMusic,
            fallbackTint = if (isFavorites) FavoriteRed
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(56.dp)
                .sharedPlaylistElement(playlistCoverKey(summary.id))
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = summary.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (summary.songCount == 1) "1 canción" else "${summary.songCount} canciones",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(mediaViewModel: MediaViewModel){

    val songs by mediaViewModel.allSongs.collectAsState();
    val playerState by mediaViewModel.playerState.collectAsState();
    val isExpanded by mediaViewModel.isExpanded.collectAsState();
    //Primer nivel: solo nombres y conteos, sin cargar canciones
    val playlistSummaries by mediaViewModel.playlistSummaries.collectAsState();
    //Segundo nivel: cual esta abierta y sus canciones
    val openPlaylist by mediaViewModel.openPlaylist.collectAsState();
    val playlistSongs by mediaViewModel.playlistSongs.collectAsState();
    //Un solo Set para toda la pantalla, en vez de una consulta por fila
    val favoriteIds by mediaViewModel.favoriteIds.collectAsState();

    //0 = nombre, 1 = artista, 2 = recientes, 3 = playlists
    var selectedFilter by rememberSaveable { mutableIntStateOf(2) }
    val groupedByPlaylist = selectedFilter == 3

    //Dentro de una playlist, el boton de atras cierra el segundo nivel en vez
    //de salirse de la app
    BackHandler(enabled = openPlaylist != null) {
        mediaViewModel.closePlaylist()
    }

    //Al salir del filtro Playlists se cierra lo que hubiera abierto, para no
    //regresar despues a un segundo nivel que ya no corresponde
    LaunchedEffect(groupedByPlaylist) {
        if (!groupedByPlaylist) mediaViewModel.closePlaylist()
    }

    //El scroll del primer nivel se iza fuera del AnimatedContent: si viviera
    //dentro, entrar a una playlist desmontaria esa rama y al volver la lista
    //arrancaria desde arriba
    val topLevelListState = rememberLazyListState()

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    //Cual playlist se esta por borrar. Null = ningun dialogo abierto.
    var playlistToDelete by remember { mutableStateOf<PlaylistSummary?>(null) }

    //Lo mismo para la cancion, pero aqui el borrado se lleva el archivo
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    songToDelete?.let { target ->
        DeleteSongDialog(
            title = target.title,
            onDismiss = { songToDelete = null },
            onConfirm = {
                mediaViewModel.deleteSong(target)
                songToDelete = null
            }
        )
    }

    playlistToDelete?.let { target ->
        DeletePlaylistDialog(
            name = target.name,
            onDismiss = { playlistToDelete = null },
            onConfirm = {
                target.id?.let { mediaViewModel.deletePlaylist(it) }
                playlistToDelete = null
            }
        )
    }

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

        //targetState es openPlaylist y NO el resumen: el resumen es un objeto
        //nuevo en cada emision del Flow, asi que la animacion se relanzaria
        //cada vez que cambiara cualquier cosa de la base. PlaylistSelection es
        //estable por igualdad.
        AnimatedContent(
            targetState = openPlaylist,
            modifier = Modifier.weight(1f),
            //Fundido suave a proposito: el protagonismo lo lleva la caratula
            //que viaja, y una transicion fuerte encima compite con ella
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "playlist-level"
        ) { selection ->

            //Sin este scope, sharedPlaylistElement se vuelve transparente y la
            //caratula aparece de golpe en lugar de crecer
            CompositionLocalProvider(LocalPlaylistAnimatedScope provides this) {

                //Se busca con selection y no con openPlaylist: mientras esta
                //rama se desvanece, openPlaylist ya cambio
                val openSummary = selection?.let { sel ->
                    playlistSummaries.firstOrNull { summary ->
                        when (sel) {
                            is PlaylistSelection.Favorites -> summary.id == null
                            is PlaylistSelection.Real -> summary.id == sel.id
                        }
                    }
                }

                //Al cerrar, el Flow emite vacio de inmediato y el detalle que
                //sale parpadearia a cero canciones. Mientras esta rama es la
                //activa sigue en vivo; cuando deja de serlo, se congela.
                var branchSongs by remember { mutableStateOf(playlistSongs) }
                if (selection == openPlaylist) branchSongs = playlistSongs

                //Los dos remember van sin condicion: un remember dentro de un
                //if cambia de posicion en la slot table si la condicion cambia,
                //y el estado se pierde o se confunde. La eleccion va despues.
                val detailListState = rememberLazyListState()
                val listState = if (openSummary == null) topLevelListState else detailListState

                Column(Modifier.fillMaxSize()) {

                //Dentro de una playlist el encabezado se sustituye: las pildoras
                //ordenan la biblioteca completa y aqui no aplican
                if (openSummary == null) {

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
                                if (playlistSummaries.size == 1) "1 lista" else "${playlistSummaries.size} listas"
                            } else {
                                if (songs.size == 1) "1 canción" else "${songs.size} canciones"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(18.dp))

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
                }

                LazyColumn(
                    Modifier.weight(1f),
                    //El izado conserva el scroll del primer nivel al ir y venir;
                    //el del detalle es propio y arranca desde arriba
                    state = listState,
                    //En el detalle no hay margen lateral: la caratula tiene que
                    //llegar a las orillas. Las canciones se lo ponen ellas.
                    contentPadding = if (openSummary == null) {
                        PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    } else {
                        PaddingValues(bottom = 4.dp)
                    },
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    //SEGUNDO NIVEL: las canciones de la playlist abierta
                    if (openSummary != null) {

                        val playSource = openSummary.id
                            ?.let { PlaySource.playlist(it) }
                            ?: PlaySource.FAVORITES

                        item(key = "detail-header") {
                            PlaylistDetailHeader(
                                summary = openSummary,
                                songs = branchSongs,
                                onBack = { mediaViewModel.closePlaylist() },
                                onPlay = {
                                    mediaViewModel.playSongs(branchSongs, 0, playSource)
                                },
                                onShuffle = {
                                    mediaViewModel.setShuffle(true)
                                    mediaViewModel.playSongs(
                                        branchSongs,
                                        Random.nextInt(branchSongs.size),
                                        playSource
                                    )
                                }
                            )
                        }

                        if (branchSongs.isEmpty()) {
                            item(key = "empty-playlist") {
                                Text(
                                    text = "Todavía no hay canciones aquí. Agrégalas desde el reproductor.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
                                )
                            }
                        }

                        itemsIndexed(branchSongs, key = { _, song -> song.id }) { index, song ->
                            //El margen va afuera para que el fondo rojo y la
                            //tarjeta queden alineados al deslizar
                            Box(Modifier.padding(horizontal = 12.dp)) {
                                SwipeToRemove(
                                    onRemove = {
                                        val id = openSummary.id
                                        if (id == null) {
                                            mediaViewModel.removeFavorite(song.id)
                                        } else {
                                            mediaViewModel.removeSongFromPlaylist(id, song.id)
                                        }
                                    }
                                ) {
                                    MediaSongCard(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable(onClick = {
                                                mediaViewModel.playSongs(
                                                    branchSongs, index, playSource
                                                )
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
                        }

                    //PRIMER NIVEL: solo los nombres de las playlists
                    } else if (groupedByPlaylist) {

                        item(key = "new-playlist") {
                            NewPlaylistRow(onClick = { showCreateDialog = true })
                        }

                        items(playlistSummaries, key = { it.id ?: -1L }) { summary ->
                            SwipeToRemove(
                                
                                enabled = summary.id != null,
                                label = "Eliminar",
                                dismissOnSwipe = false,
                                onRemove = { playlistToDelete = summary }
                            ) {
                                PlaylistSummaryRow(
                                    summary = summary,
                                    onClick = {
                                        mediaViewModel.openPlaylist(
                                            if (summary.id == null) PlaylistSelection.Favorites
                                            else PlaylistSelection.Real(summary.id)
                                        )
                                    }
                                )
                            }
                        }

                    } else {
                        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                            //Aqui deslizar borra el archivo, a diferencia del
                            //detalle de playlist donde solo la saca de la lista
                            SwipeToRemove(
                                label = "Eliminar",
                                //La fila regresa; el archivo se borra solo si
                                //se confirma en el dialogo
                                dismissOnSwipe = false,
                                onRemove = { songToDelete = song }
                            ) {
                                MediaSongCard(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        //Opaca para que al deslizar no se vea
                                        //el rojo a traves de la tarjeta
                                        .background(MaterialTheme.colorScheme.primary)
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
                }

                } //cierra el Column interno de la rama
            } //cierra el CompositionLocalProvider
        } //cierra el AnimatedContent

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
