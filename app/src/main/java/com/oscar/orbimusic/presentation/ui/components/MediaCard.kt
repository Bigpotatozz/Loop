package com.oscar.orbimusic.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.presentation.ui.theme.OrbiMusicTheme
import com.oscar.orbimusic.presentation.utils.parseTime

@Composable
fun EqualizerAnimation(isPlaying: Boolean) {
    // Una sola transicion para las tres barras; cada una deriva su propia altura
    val transition = rememberInfiniteTransition(label = "equalizer")

    Row(
        modifier = Modifier.size(width = 18.dp, height = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(3) { index ->
            val height by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 400 + (index * 150), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight$index"
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(if (isPlaying) height else 0.25f)
                    .clip(RoundedCornerShape(1.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

//Cuadro que sustituye a la caratula cuando la cancion no tiene imagen.
//Los colores son parametros porque tambien se usa sobre el fondo negro del mini-player
@Composable
fun AlbumArtPlaceholder(
    title: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.outline,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = 22.sp
) {
    Box(
        modifier.background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.trim().take(1).uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            ),
            color = contentColor
        )
    }
}

@Composable
fun MediaSongCard(
    modifier: Modifier = Modifier,
    title: String,
    artist: String,
    albumArt: String?,
    duration: Long,
    isPlaying: Boolean = false
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .background(
                if (isPlaying) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val artShape = RoundedCornerShape(8.dp)
        val artModifier = if (isPlaying) {
            Modifier
                .size(56.dp)
                .clip(artShape)
                .border(2.dp, MaterialTheme.colorScheme.secondary, artShape)
        } else {
            Modifier
                .size(56.dp)
                .clip(artShape)
        }

        if (albumArt == null) {
            AlbumArtPlaceholder(title, artModifier)
        } else {
            AsyncImage(
                model = albumArt,
                contentDescription = title,
                modifier = artModifier,
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = if (isPlaying) FontWeight.SemiBold else FontWeight.Medium,
                    letterSpacing = (-0.2).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(12.dp))

        //Ancho fijo para que las filas no se muevan al alternar duracion/ecualizador
        Box(Modifier.width(28.dp), contentAlignment = Alignment.CenterEnd) {
            if (isPlaying) {
                EqualizerAnimation(isPlaying = true)
            } else {
                Text(
                    text = parseTime(duration),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MediaSongCardPreview() {
    OrbiMusicTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            MediaSongCard(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                title = "Smells Like Teen Spirit",
                artist = "Nirvana",
                albumArt = null,
                duration = 301000L,
                isPlaying = true
            )
            MediaSongCard(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                title = "Come As You Are",
                artist = "Nirvana",
                albumArt = null,
                duration = 219000L
            )
            MediaSongCard(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)),
                title = "Something In The Way",
                artist = "Nirvana",
                albumArt = null,
                duration = 232000L
            )
        }
    }
}
