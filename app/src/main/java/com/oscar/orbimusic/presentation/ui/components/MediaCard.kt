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
                Modifier.size(35.dp).clip(RoundedCornerShape(6.dp)))

            Spacer(Modifier.padding(5.dp))
            Column(Modifier.fillMaxWidth(),verticalArrangement = Arrangement.Center) {
                Text(title, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                Text(artist,  style = TextStyle(fontSize = 8.sp))
            }

        }

        Column() {
            Text("${minutes}:${seconds}", style = TextStyle(fontSize = 15.sp))
        }

    }


};

@Preview
@Composable
fun MediaSongCardPreview(){

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row( Modifier.weight(0.8f) ,
            verticalAlignment = Alignment.CenterVertically) {

            Image(painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "imagen",
                Modifier.size(35.dp).clip(RoundedCornerShape(6.dp)))

            Spacer(Modifier.padding(5.dp))
            Column(Modifier.fillMaxWidth(),verticalArrangement = Arrangement.Center) {
                Text("Smells Like Teen Spirit", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                Text("Nirvana",  style = TextStyle(fontSize = 8.sp))
            }

        }

        Column() {
            Text("3:00", style = TextStyle(fontSize = 15.sp))
        }




    }

};