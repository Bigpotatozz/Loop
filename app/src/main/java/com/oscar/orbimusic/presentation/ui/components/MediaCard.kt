package com.oscar.orbimusic.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.R
import com.oscar.orbimusic.domain.model.Song


@Composable

fun MediaSongCard(title: String, artist: String, albumArt: String?, duration: Long){

    val durationInSeconds = duration / 1000
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row( Modifier.weight(0.8f) ,
            verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(model = albumArt,
                contentDescription = title,
                Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)))

            Spacer(Modifier.padding(5.dp))
            Column(Modifier.fillMaxWidth(),verticalArrangement = Arrangement.Center) {
                Text(title,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                Text(artist,
                    style = TextStyle(fontSize = 15.sp), color = MaterialTheme.colorScheme.tertiary)
            }

        }

        Column() {
            Text("${minutes}:${seconds}", style = TextStyle(fontSize = 15.sp))
        }

    }


};

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MediaSongCardPreview(){
    Column(Modifier.fillMaxWidth().padding(top = 100.dp)) {
        MediaSongCard(
            title = "Smells Like Teen Spirit",
            artist = "Nirvana",
            albumArt = null,
            duration = 180000L
        )
    }
}