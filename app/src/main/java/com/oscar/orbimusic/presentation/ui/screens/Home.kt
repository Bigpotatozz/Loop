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
import coil.compose.AsyncImage
import com.oscar.orbimusic.presentation.models.TopAlbumsState
import com.oscar.orbimusic.presentation.viewmodel.HomeViewModel
import com.oscar.orbimusic.presentation.models.TrendingState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.Locale
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
private val CARD_GAP = 10
    .dp
private val CAROUSEL_HEIGHT = 210.dp

//Tarjeta de artista: rectangulo horizontal. Alta como una tarjeta de album
//completa (portada + titulo + subtitulo) para que las dos secciones pesen igual
private val ARTIST_CARD_WIDTH = 300.dp
private val ARTIST_CARD_HEIGHT = 180.dp
private val ARTIST_PHOTO_WIDTH = 170.dp

private fun greetingForHour(hour: Int): String = when (hour) {
    in 6..11 -> "Buenos días"
    in 12..19 -> "Buenas tardes"
    else -> "Buenas noches"
}

@Composable
fun HomeScreen(homeViewModel: HomeViewModel, onAlbumClick: (Long) -> Unit){

    val topAlbums by homeViewModel.topAlbums.collectAsState()
    val trending by homeViewModel.trending.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        val saludo = remember { greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 20.dp)) {
            Text(
                text = saludo,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            )
        }


        SectionTitle("Top 10 álbumes")

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
                            fanCount = artist.fanCount
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

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

private fun formatFans(fans: Int): String = when {
    fans >= 1_000_000 -> {
        val millions = fans / 1_000_000.0
        //Un decimal solo si aporta: "12 M" y no "12.0 M"
        val text = if (millions >= 10) millions.toInt().toString()
        else String.format(Locale.US, "%.1f", millions).removeSuffix(".0")
        "$text M fans"
    }
    fans >= 1_000 -> "${fans / 1_000} mil fans"
    fans == 1 -> "1 fan"
    else -> "$fans fans"
}

@Composable
private fun ArtistCard(name: String, pictureUrl: String, fanCount: Int?) {
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
private fun CarouselCard(coverUrl: String, title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier
            .width(CARD_SIZE)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {

        AsyncImage(
            model = coverUrl,
            contentDescription = title,
            modifier = Modifier
                .size(CARD_SIZE)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

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
