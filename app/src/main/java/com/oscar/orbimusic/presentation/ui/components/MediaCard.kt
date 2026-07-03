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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun EqualizerAnimation(isPlaying: Boolean) {
    Row(
        modifier = Modifier.size(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(3) { index ->
            val transition = rememberInfiniteTransition(label = "equalizer")
            val height by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 400 + (index * 150), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight"
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(if (isPlaying) height else 0.2f)
                    .clip(RoundedCornerShape(1.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
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
    val durationInSeconds = duration / 1000
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(

                model = albumArt,
                contentDescription = title,
                modifier = if(isPlaying) Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(10.dp))
                else Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isPlaying) {
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {

            if(isPlaying){
                EqualizerAnimation(isPlaying = true)
            }else{
                Text(
                    text = String.format("%d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                )
            }

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MediaSongCardPreview() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        MediaSongCard(
            title = "Smells Like Teen Spirit",
            artist = "Nirvana",
            albumArt = null,
            duration = 180000L,
            isPlaying = true
        )
    }
}
