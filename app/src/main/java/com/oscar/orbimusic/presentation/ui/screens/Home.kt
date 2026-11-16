package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn
import coil.compose.AsyncImage
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.QueueMusic
import com.oscar.orbimusic.domain.models.PlaylistSelection
import com.oscar.orbimusic.domain.models.PlaylistSummary
import com.oscar.orbimusic.presentation.models.TopAlbumsState
import com.oscar.orbimusic.presentation.ui.components.DownloadRowState
import com.oscar.orbimusic.presentation.ui.theme.FavoriteRed
import com.oscar.orbimusic.presentation.ui.components.DownloadStateButton
import com.oscar.orbimusic.presentation.ui.icons.PlayerIcons
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel
import com.oscar.orbimusic.presentation.viewmodel.HomeViewModel
import com.oscar.orbimusic.presentation.models.TrendingState
import com.oscar.orbimusic.presentation.models.TopTracksState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Calendar
import com.oscar.orbimusic.presentation.utils.formatFans
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CARD_SIZE = 140.dp
internal val CARD_GAP = 10.dp
private val CAROUSEL_HEIGHT = 210.dp

//Tarjeta de artista: rectangulo horizontal. Alta como una tarjeta de album
//completa (portada + titulo + subtitulo) para que las dos secciones pesen igual
private val ARTIST_CARD_WIDTH = 300.dp
private val ARTIST_CARD_HEIGHT = 180.dp
private val ARTIST_PHOTO_WIDTH = 170.dp

private fun Modifier.clickableIfNotNull(onClick: (() -> Unit)?): Modifier =
    if (onClick == null) this else this.clickable(onClick = onClick)

private fun greetingForHour(hour: Int): String = when (hour) {
    in 6..11 -> "Buenos días"
    in 12..19 -> "Buenas tardes"
    else -> "Buenas noches"
}

@OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    downloadViewModel: DownloadViewModel,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    isMainPlaying: Boolean,
    onPauseMain: () -> Unit,
    playlists: List<PlaylistSummary>,
    onOpenPlaylist: (PlaylistSelection) -> Unit,
    onCreatePlaylist: () -> Unit
){

    val topAlbums by homeViewModel.topAlbums.collectAsState()
    val trending by homeViewModel.trending.collectAsState()
    val topTracks by homeViewModel.topTracks.collectAsState()
    val chartCountry by homeViewModel.chartCountry.collectAsState()

    val previewTrackId by homeViewModel.previewTrackId.collectAsState()
    val isPreviewLoading by homeViewModel.isPreviewLoading.collectAsState()

    val isDownloading by downloadViewModel.isDownloading.collectAsState()
    val downloadingId by downloadViewModel.downloadingId.collectAsState()
    val downloadProgress by downloadViewModel.downloadProgress.collectAsState()
    val completedDownloads by downloadViewModel.completedDownloads.collectAsState()
    val downloadError by downloadViewModel.downloadError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isMainPlaying) {
        if (isMainPlaying) homeViewModel.stopPreview()
    }

    DisposableEffect(Unit) {
        onDispose { homeViewModel.stopPreview() }
    }

    LaunchedEffect(downloadError) {
        downloadError?.let { message ->
            snackbarHostState.showSnackbar(message)
            downloadViewModel.clearDownloadError()
        }
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        val saludo = remember { greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = saludo,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
            )

            ThemeSwitch(isDarkTheme = isDarkTheme, onDarkThemeChange = onDarkThemeChange)
        }




        Spacer(Modifier.height(16.dp))

        SectionTitle("Top 10 artistas")

        when (val state = trending) {
            TrendingState.Loading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(ARTIST_CARD_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }

            is TrendingState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                )
            }

            is TrendingState.Ready -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(CARD_GAP)
                ) {
                    items(state.artists, key = { it.id }) { artist ->
                        ArtistCard(
                            name = artist.name,
                            pictureUrl = artist.pictureXl,
                            fanCount = artist.fanCount,
                            onClick = { onArtistClick(artist.id) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))


        SectionTitle(nationalTitle("Top 10 canciones", chartCountry))

        when (val state = topTracks) {
            TopTracksState.Loading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(CAROUSEL_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }

            is TopTracksState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                )
            }

            is TopTracksState.Ready -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(CARD_GAP)
                ) {
                    items(state.tracks, key = { it.id }) { track ->
                        val rowKey = downloadViewModel.deezerRowKey(track.id)
                        val rowState = when {
                            rowKey in completedDownloads -> DownloadRowState.Done
                            rowKey != downloadingId -> DownloadRowState.Idle
                            downloadProgress < 0 -> DownloadRowState.Starting
                            else -> DownloadRowState.Running(downloadProgress)
                        }

                        CarouselCard(
                            coverUrl = track.coverXl,
                            title = track.title,
                            subtitle = track.artistName,
                            onPreviewClick = track.previewUrl?.let {
                                {
                                    if (isMainPlaying) onPauseMain()
                                    homeViewModel.togglePreview(track.id)
                                }
                            },
                            isPreviewing = previewTrackId == track.id && !isPreviewLoading,
                            isPreviewLoading = previewTrackId == track.id && isPreviewLoading,
                            downloadState = rowState,
                            downloadEnabled = !isDownloading,
                            onDownload = {
                                downloadViewModel.downloadDeezerTrack(
                                    trackId = track.id,
                                    artist = track.artistName,
                                    title = track.title
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle(nationalTitle("Top 10 álbumes", chartCountry))

        when (val state = topAlbums) {
            TopAlbumsState.Loading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(CAROUSEL_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            }

            is TopAlbumsState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                )
            }

            is TopAlbumsState.Ready -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(CARD_GAP)
                ) {
                    items(state.albums, key = { it.id }) { album ->
                        CarouselCard(
                            coverUrl = album.coverXl,
                            title = album.title,
                            subtitle = album.artistName,
                            onClick = { onAlbumClick(album.id) }
                        )
                    }
                }
            }
        }


        Spacer(Modifier.height(16.dp))

        SectionTitle("Tus playlists")

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(CARD_GAP)
        ) {
            items(playlists, key = { it.id ?: -1L }) { playlist ->
                val isFavorites = playlist.id == null

                CarouselCard(
                    title = playlist.name,
                    subtitle = if (playlist.songCount == 1) "1 canción"
                    else "${playlist.songCount} canciones",
                    onClick = {
                        onOpenPlaylist(
                            if (isFavorites) PlaylistSelection.Favorites
                            else PlaylistSelection.Real(playlist.id!!)
                        )
                    },
                    coverContent = { modifier ->
                        PlaylistCoverMosaic(
                            coverPaths = playlist.coverPaths,
                            fallbackIcon = if (isFavorites) Icons.Rounded.Favorite
                            else Icons.Rounded.QueueMusic,
                            fallbackTint = if (isFavorites) FavoriteRed
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = modifier,
                            fallbackIconSize = 40.dp
                        )
                    }
                )
            }

            item(key = "new-playlist") {
                NewPlaylistCard(onClick = onCreatePlaylist)
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
    } // Box
}

//Tarjeta para crear una playlist desde el Home. Mantiene el tamaño y la forma de
//CarouselCard para que no rompa la fila, pero con un cuadro punteado en vez de
//portada: asi se lee como una accion y no como una playlist mas.
@Composable
private fun NewPlaylistCard(onClick: () -> Unit) {
    Column(
        Modifier
            .width(CARD_SIZE)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .size(CARD_SIZE)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Nueva playlist",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Nueva playlist",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

//Switch del tema de la app.
@Composable
private fun ThemeSwitch(isDarkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    Switch(
        checked = isDarkTheme,
        onCheckedChange = onDarkThemeChange,
        thumbContent = {
            Icon(
                imageVector = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                contentDescription = if (isDarkTheme) "Cambiar a modo claro"
                else "Cambiar a modo oscuro",
                modifier = Modifier.size(SwitchDefaults.IconSize)
            )
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedIconColor = MaterialTheme.colorScheme.secondary,
            checkedTrackColor = MaterialTheme.colorScheme.secondary,
            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
            uncheckedIconColor = MaterialTheme.colorScheme.primary,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

private fun nationalTitle(base: String, country: String?): String =
    if (country.isNullOrBlank()) base else "$base en $country"

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = (-0.3).sp
        ),
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
    )
}

@Composable
private fun ArtistCard(name: String, pictureUrl: String, fanCount: Int?, onClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //Null hasta que Palette termina; mientras, la tarjeta usa el color neutro
    var extracted by remember(pictureUrl) { mutableStateOf<Color?>(null) }

    //Transicion suave del gris al tono de la foto en vez de un salto
    val cardColor by animateColorAsState(
        targetValue = extracted ?: MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(400),
        label = "artistCardColor"
    )

    //El texto tiene que leerse sobre cualquier tono: blanco en fondos oscuros,
    //negro en claros. Sin color extraido se usan los del tema.
    val titleColor = extracted?.let { if (it.luminance() > 0.5f) Color.Black else Color.White }
        ?: MaterialTheme.colorScheme.onPrimary
    val subtitleColor = extracted?.let { titleColor.copy(alpha = 0.75f) }
        ?: MaterialTheme.colorScheme.onSurfaceVariant

    //allowHardware(false): los bitmaps de hardware viven en la GPU y Palette no
    //puede leer sus pixeles
    val request = remember(pictureUrl) {
        ImageRequest.Builder(context)
            .data(pictureUrl)
            .allowHardware(false)
            .build()
    }

    Box(
        Modifier
            .size(width = ARTIST_CARD_WIDTH, height = ARTIST_CARD_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            //Despues del clip para que el ripple respete las esquinas
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = request,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            onSuccess = { success ->
                val bitmap = (success.result.drawable as? BitmapDrawable)?.bitmap
                    ?: return@AsyncImage
                //Palette recorre pixeles: fuera del hilo principal
                scope.launch {
                    extracted = withContext(Dispatchers.Default) {
                        val palette = Palette.from(bitmap).generate()
                        (palette.dominantSwatch ?: palette.mutedSwatch)
                            ?.rgb?.let { Color(it) }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(ARTIST_PHOTO_WIDTH)
                //Degradado horizontal
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.horizontalGradient(
                            colors = listOf(cardColor, Color.Transparent),
                            startX = 0f,
                            endX = size.width * 0.45f
                        )
                    )
                }
        )

        Column(
            Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp, end = ARTIST_PHOTO_WIDTH - 30.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 21.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = titleColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (fanCount != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatFans(fanCount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = subtitleColor,
                    maxLines = 1
                )
            }
        }
    }
}

//Caratula grande con el titulo y un subtitulo debajo.
@Composable
//onClick null = tarjeta sin accion (las canciones, por ahora)
//internal: ArtistScreen la usa para el carrusel de albumes
internal fun CarouselCard(
    coverUrl: String = "",
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    onPreviewClick: (() -> Unit)? = null,
    isPreviewing: Boolean = false,
    isPreviewLoading: Boolean = false,
    downloadState: DownloadRowState? = null,
    downloadEnabled: Boolean = true,
    onDownload: (() -> Unit)? = null,
    coverContent: (@Composable (Modifier) -> Unit)? = null
) {
    Column(
        Modifier
            .width(CARD_SIZE)
            .clip(RoundedCornerShape(12.dp))
            .clickableIfNotNull(onClick)
    ) {

        Box(Modifier.size(CARD_SIZE)) {
            val coverModifier = Modifier
                .size(CARD_SIZE)
                .clip(RoundedCornerShape(12.dp))
                .clickableIfNotNull(onPreviewClick)

            if (coverContent != null) {
                coverContent(coverModifier)
            } else {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = title,
                    modifier = coverModifier,
                    contentScale = ContentScale.Crop
                )
            }

            if (isPreviewing || isPreviewLoading) {
                Box(
                    Modifier
                        .size(CARD_SIZE)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPreviewLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = PlayerIcons.Pause,
                            contentDescription = "Detener preview",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            if (downloadState != null && onDownload != null) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    DownloadStateButton(
                        state = downloadState,
                        title = title,
                        enabled = downloadEnabled,
                        onDownload = onDownload,
                        tint = Color.White,
                        buttonSize = 32.dp,
                        iconSize = 18.dp
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
