package com.oscar.orbimusic.presentation.ui.screens

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
import java.util.Calendar

private val CARD_SIZE = 140.dp
private val CARD_GAP = 10
    .dp
private val CAROUSEL_HEIGHT = 210.dp

private fun greetingForHour(hour: Int): String = when (hour) {
    in 6..11 -> "Buenos días"
    in 12..19 -> "Buenas tardes"
    else -> "Buenas noches"
}

@Composable
fun HomeScreen(homeViewModel: HomeViewModel){

    val topAlbums by homeViewModel.topAlbums.collectAsState()

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


        Text(
            text = "Top 10 álbumes",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = (-0.3).sp
            ),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
        )

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
                            subtitle = album.artistName
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

//Caratula grande con el titulo y un subtitulo debajo.
@Composable
private fun CarouselCard(coverUrl: String, title: String, subtitle: String) {
    Column(Modifier.width(CARD_SIZE)) {

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
