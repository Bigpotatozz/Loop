package com.oscar.orbimusic.presentation.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.oscar.orbimusic.domain.models.Album
import com.oscar.orbimusic.domain.models.Artist
import com.oscar.orbimusic.domain.models.ArtistDetail
import com.oscar.orbimusic.presentation.models.ArtistState
import com.oscar.orbimusic.presentation.ui.components.DownloadRowState
import com.oscar.orbimusic.presentation.utils.formatFans
import com.oscar.orbimusic.presentation.viewmodel.ArtistScreenViewModel
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel

private const val COLLAPSED_COUNT = 5

//Foto de los artistas similares
private val RELATED_PHOTO_SIZE = 110.dp

@OptIn(UnstableApi::class)
@Composable
fun ArtistScreen(
    artistId: Long,
    artistViewModel: ArtistScreenViewModel,
    downloadViewModel: DownloadViewModel,
    isMainPlaying: Boolean,
    onPauseMain: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(artistId) { artistViewModel.load(artistId) }

    val state by artistViewModel.artist.collectAsState()

    val previewTrackId by artistViewModel.previewTrackId.collectAsState()
    val isPreviewLoading by artistViewModel.isPreviewLoading.collectAsState()
    val previewError by artistViewModel.previewError.collectAsState()

    //Al reves: si la biblioteca vuelve a sonar, el preview se calla
    LaunchedEffect(isMainPlaying) {
        if (isMainPlaying) artistViewModel.stopPreview()
    }

    //Al salir de la pantalla el preview se detiene aunque el ViewModel siga
    //vivo en el back stack
    DisposableEffect(Unit) {
        onDispose { artistViewModel.stopPreview() }
    }

    val isDownloading by downloadViewModel.isDownloading.collectAsState()
    val downloadingId by downloadViewModel.downloadingId.collectAsState()
    val downloadProgress by downloadViewModel.downloadProgress.collectAsState()
    val completedDownloads by downloadViewModel.completedDownloads.collectAsState()
    val downloadError by downloadViewModel.downloadError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var expanded by rememberSaveable(artistId) { mutableStateOf(false) }

    LaunchedEffect(downloadError) {
        downloadError?.let { message ->
            snackbarHostState.showSnackbar(message)
            downloadViewModel.clearDownloadError()
        }
    }

    LaunchedEffect(previewError) {
        previewError?.let { message ->
            snackbarHostState.showSnackbar(message)
            artistViewModel.clearPreviewError()
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (val current = state) {
            ArtistState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    AlbumBackButton(onBack)
                }
            }

            is ArtistState.Error -> {
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
                        TextButton(onClick = { artistViewModel.retry() }) {
                            Text("Reintentar", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    AlbumBackButton(onBack)
                }
            }

            is ArtistState.Ready -> {
                val artist = current.artist
                val canExpand = artist.topTracks.size > COLLAPSED_COUNT
                val visibleTracks =
                    if (expanded) artist.topTracks else artist.topTracks.take(COLLAPSED_COUNT)

                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    //La cabecera se va con el scroll, igual que en el album
                    item(key = "artist-header") {
                        ArtistDetailHeader(artist = artist, onBack = onBack)
                    }

                    item(key = "popular-title") {
                        Text(
                            text = "Canciones populares",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                        )
                    }

                    //Lista vacia = /top fallo o el artista no tiene canciones;
                    //el repositorio no tumba la pantalla por eso
                    if (artist.topTracks.isEmpty()) {
                        item(key = "empty-top") {
                            Text(
                                text = "No hay canciones populares disponibles.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
                            )
                        }
                    }

                    items(visibleTracks, key = { it.id }) { track ->
                        val rowKey = downloadViewModel.deezerRowKey(track.id)

                        //Misma logica que AlbumScreen: un solo progreso,
                        //downloadingId dice a que fila le toca
                        val rowState = when {
                            rowKey in completedDownloads -> DownloadRowState.Done
                            rowKey != downloadingId -> DownloadRowState.Idle
                            downloadProgress < 0 -> DownloadRowState.Starting
                            else -> DownloadRowState.Running(downloadProgress)
                        }

                        AlbumTrackRow(
                            track = track,
                            //Solo en colaboraciones: si es el mismo artista de
                            //la pantalla repetirlo en cada fila es ruido
                            showArtist = track.artistName != artist.name,
                            isPreviewing = previewTrackId == track.id,
                            isPreviewLoading = previewTrackId == track.id && isPreviewLoading,
                            onPreview = track.previewUrl?.let {
                                {
                                    if (isMainPlaying) onPauseMain()
                                    artistViewModel.togglePreview(track.id)
                                }
                            },
                            downloadState = rowState,
                            downloadEnabled = !isDownloading,
                            onDownload = {
                                downloadViewModel.downloadDeezerTrack(
                                    trackId = track.id,
                                    artist = track.artistName,
                                    title = track.title
                                )
                            },
                            modifier = Modifier.padding(horizontal = 2.dp),
                            coverUrl = track.coverMedium
                        )
                    }

                    if (canExpand) {
                        item(key = "toggle-more") {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                //Chip gris: el fondo y la flecha dicen que se toca
                                Row(
                                    Modifier
                                        .clip(RoundedCornerShape(100))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { expanded = !expanded }
                                        .padding(start = 16.dp, end = 10.dp, top = 7.dp, bottom = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (expanded) "Ver menos" else "Ver más",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp
                                        else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    //Sin albumes (o si /albums fallo) la seccion no sale
                    if (artist.albums.isNotEmpty()) {
                        item(key = "albums-title") {
                            Text(
                                text = "Álbumes",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 8.dp)
                            )
                        }

                        item(key = "albums-carousel") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(CARD_GAP)
                            ) {
                                items(artist.albums, key = { it.id }) { album ->
                                    CarouselCard(
                                        coverUrl = album.coverXl,
                                        title = album.title,
                                        subtitle = albumCardSubtitle(album),
                                        onClick = { onAlbumClick(album.id) }
                                    )
                                }
                            }
                        }
                    }

                    if (artist.related.isNotEmpty()) {
                        item(key = "related-title") {
                            Text(
                                text = "Artistas similares",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 8.dp)
                            )
                        }

                        item(key = "related-carousel") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(artist.related, key = { it.id }) { related ->
                                    RelatedArtistCard(
                                        artist = related,
                                        onClick = { onArtistClick(related.id) }
                                    )
                                }
                            }
                        }
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


@Composable
private fun ArtistDetailHeader(artist: ArtistDetail, onBack: () -> Unit) {
    //El degradado funde con el fondo real de la pantalla, que es primary
    val background = MaterialTheme.colorScheme.primary

    Column(Modifier.fillMaxWidth()) {

        Box(Modifier.fillMaxWidth()) {
            AsyncImage(
                model = artist.pictureXl,
                contentDescription = artist.name,
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
                text = artist.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val summary = artistSummary(artist)
            if (summary.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}


//Foto circular con el nombre y los fans debajo, centrados. Ancho fijo para
//que los nombres largos se corten en vez de ensanchar la tarjeta.
@Composable
internal fun RelatedArtistCard(artist: Artist, onClick: () -> Unit) {
    Column(
        Modifier
            .width(RELATED_PHOTO_SIZE)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = artist.pictureMedium,
            contentDescription = artist.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(RELATED_PHOTO_SIZE)
                .clip(CircleShape)
                //Placeholder mientras carga, para que no se vea un hueco
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        artist.fanCount?.takeIf { it > 0 }?.let { fans ->
            Text(
                text = formatFans(fans),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}


//"2024 · Álbum" o "2023 · EP". Lo que no venga se omite.
private fun albumCardSubtitle(album: Album): String {
    val parts = mutableListOf<String>()
    album.releaseDate?.take(4)?.takeIf { it.length == 4 }?.let { parts.add(it) }
    when (album.recordType) {
        "album" -> parts.add("Álbum")
        "ep" -> parts.add("EP")
    }
    return parts.joinToString(" · ")
}


private fun artistSummary(artist: ArtistDetail): String {
    val parts = mutableListOf<String>()

    artist.fanCount?.takeIf { it > 0 }?.let { parts.add(formatFans(it)) }

    artist.albumCount?.takeIf { it > 0 }?.let {
        parts.add(if (it == 1) "1 álbum" else "$it álbumes")
    }

    return parts.joinToString(" · ")
}
