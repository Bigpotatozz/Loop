package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.domain.models.AlbumDetail
import com.oscar.orbimusic.domain.models.Track
import com.oscar.orbimusic.presentation.models.AlbumState
import com.oscar.orbimusic.presentation.utils.parseTime
import com.oscar.orbimusic.presentation.utils.parseTotalTime
import com.oscar.orbimusic.presentation.viewmodel.AlbumScreenViewModel
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel
import com.oscar.orbimusic.presentation.ui.components.DownloadRowState
import com.oscar.orbimusic.presentation.ui.components.DownloadStateButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.background
import com.oscar.orbimusic.presentation.ui.components.EqualizerAnimation
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

//DownloadViewModel esta marcado @UnstableApi, igual que en DownloadScreen
@OptIn(UnstableApi::class)
@Composable
fun AlbumScreen(
    albumId: Long,
    albumViewModel: AlbumScreenViewModel,
    downloadViewModel: DownloadViewModel,
    //Si la musica de la biblioteca esta sonando, para pausarla al lanzar un
    //preview: el player principal no maneja audio focus y sonarian los dos
    isMainPlaying: Boolean,
    onPauseMain: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(albumId) { albumViewModel.load(albumId) }

    val state by albumViewModel.album.collectAsState()

    val previewTrackId by albumViewModel.previewTrackId.collectAsState()
    val isPreviewLoading by albumViewModel.isPreviewLoading.collectAsState()
    val previewError by albumViewModel.previewError.collectAsState()

    //Al revés: si la biblioteca vuelve a sonar (mini player, notificacion),
    //el preview se calla
    LaunchedEffect(isMainPlaying) {
        if (isMainPlaying) albumViewModel.stopPreview()
    }

    //Al salir de la pantalla (atras o cambiar de pestaña) el preview se
    //detiene. El ViewModel puede seguir vivo en el back stack, el sonido no.
    DisposableEffect(Unit) {
        onDispose { albumViewModel.stopPreview() }
    }

    val isDownloading by downloadViewModel.isDownloading.collectAsState()
    val downloadingId by downloadViewModel.downloadingId.collectAsState()
    val downloadProgress by downloadViewModel.downloadProgress.collectAsState()
    val completedDownloads by downloadViewModel.completedDownloads.collectAsState()
    val downloadError by downloadViewModel.downloadError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    //Mismo manejo que DownloadScreen: se muestra y se limpia para que no
    //vuelva a salir en la siguiente recomposicion
    LaunchedEffect(downloadError) {
        downloadError?.let { message ->
            snackbarHostState.showSnackbar(message)
            downloadViewModel.clearDownloadError()
        }
    }

    LaunchedEffect(previewError) {
        previewError?.let { message ->
            snackbarHostState.showSnackbar(message)
            albumViewModel.clearPreviewError()
        }
    }

    Box(Modifier.fillMaxSize()) {
    when (val current = state) {
        AlbumState.Loading -> {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.Center)
                )
                AlbumBackButton(onBack)
            }
        }

        is AlbumState.Error -> {
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = current.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { albumViewModel.retry() }) {
                        Text("Reintentar", color = MaterialTheme.colorScheme.secondary)
                    }
                }
                AlbumBackButton(onBack)
            }
        }

        is AlbumState.Ready -> {
            val album = current.album

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                //La cabecera va como item y no fija arriba, igual que en el
                //detalle de playlist: se va con el scroll
                item(key = "album-header") {
                    AlbumDetailHeader(album = album, onBack = onBack)
                }

                if (album.tracks.isEmpty()) {
                    item(key = "empty-album") {
                        Text(
                            text = "Este álbum no tiene canciones disponibles.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
                        )
                    }
                }

                items(album.tracks, key = { it.id }) { track ->
                    val rowKey = downloadViewModel.deezerRowKey(track.id)

                    //Misma logica que las filas de DownloadScreen: un solo
                    //progreso, downloadingId dice a que fila le toca
                    val rowState = when {
                        rowKey in completedDownloads -> DownloadRowState.Done
                        rowKey != downloadingId -> DownloadRowState.Idle
                        downloadProgress < 0 -> DownloadRowState.Starting
                        else -> DownloadRowState.Running(downloadProgress)
                    }

                    AlbumTrackRow(
                        track = track,
                        //Solo se muestra el artista si difiere del del album
                        //(colaboraciones); si no, repetirlo en cada fila es ruido
                        showArtist = track.artistName != album.artistName,
                        isPreviewing = previewTrackId == track.id,
                        isPreviewLoading = previewTrackId == track.id && isPreviewLoading,
                        //Sin preview la fila no se puede tocar (pasa con
                        //algunas canciones con restriccion de pais)
                        onPreview = track.previewUrl?.let {
                            {
                                if (isMainPlaying) onPauseMain()
                                albumViewModel.togglePreview(track.id)
                            }
                        },
                        downloadState = rowState,
                        //Una descarga a la vez, igual que en la busqueda
                        downloadEnabled = !isDownloading,
                        onDownload = {
                            downloadViewModel.downloadDeezerTrack(
                                trackId = track.id,
                                artist = track.artistName,
                                title = track.title
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


//Mismo boton flotante que el detalle de playlist (Media.kt)
@Composable
private fun AlbumBackButton(onBack: () -> Unit) {
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
            contentDescription = "Regresar",
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}


//Misma estructura que PlaylistDetailHeader, con la caratula del album en vez
//del mosaico 2x2.
@Composable
private fun AlbumDetailHeader(album: AlbumDetail, onBack: () -> Unit) {
    //El degradado funde con el fondo real de la pantalla, que es primary
    val background = MaterialTheme.colorScheme.primary

    Column(Modifier.fillMaxWidth()) {

        Box(Modifier.fillMaxWidth()) {
            AsyncImage(
                model = album.coverXl,
                contentDescription = album.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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

            AlbumBackButton(onBack)
        }

        Column(Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = album.title,
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
                text = album.artistName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = albumSummary(album),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}


//"Álbum · 2024 · 12 canciones · 45 min". Lo que no venga se omite.
private fun albumSummary(album: AlbumDetail): String {
    val parts = mutableListOf<String>()

    when (album.recordType) {
        "album" -> parts.add("Álbum")
        "single" -> parts.add("Sencillo")
        "ep" -> parts.add("EP")
        "compile" -> parts.add("Recopilación")
    }

    album.releaseDate?.take(4)?.takeIf { it.length == 4 }?.let { parts.add(it) }

    val count = album.tracks.size
    parts.add(if (count == 1) "1 canción" else "$count canciones")

    //Los helpers de tiempo trabajan en ms; Deezer da segundos
    album.durationSeconds?.takeIf { it > 0 }?.let {
        parts.add(parseTotalTime(it * 1000L))
    }

    return parts.joinToString(" · ")
}

@Composable
private fun AlbumTrackRow(
    track: Track,
    showArtist: Boolean,
    isPreviewing: Boolean,
    isPreviewLoading: Boolean,
    onPreview: (() -> Unit)?,
    downloadState: DownloadRowState,
    downloadEnabled: Boolean,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(10.dp))
            //Mismo resaltado que la cancion que suena en MediaSongCard
            .background(
                if (isPreviewing) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .clickable(enabled = onPreview != null, onClick = { onPreview?.invoke() })
            //Menos a la derecha: el boton ya trae su propio aire de 48.dp
            .padding(start = 10.dp, end = 0.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Ancho fijo para que los titulos queden alineados con 1 o 2 digitos y
        //no se muevan al cambiar el numero por el spinner o el ecualizador
        Box(Modifier.width(28.dp), contentAlignment = Alignment.Center) {
            when {
                isPreviewLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary
                )

                isPreviewing -> EqualizerAnimation(isPlaying = true)

                else -> Text(
                    text = (track.position ?: 0).toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showArtist) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = track.artistName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        track.durationSeconds?.let { seconds ->
            Text(
                text = parseTime(seconds * 1000L),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(4.dp))

        DownloadStateButton(
            state = downloadState,
            title = track.title,
            enabled = downloadEnabled,
            onDownload = onDownload
        )
    }
}
