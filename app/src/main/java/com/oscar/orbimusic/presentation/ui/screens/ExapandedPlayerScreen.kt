package com.oscar.orbimusic.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.oscar.orbimusic.R
import com.oscar.orbimusic.domain.model.PlayerState
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel


/*playerState: PlayerState, mediaViewModel: MediaViewModel*/
@Composable
fun ExpandedPlayerScreen(modifier: Modifier = Modifier,
                         title: String,
                         artist: String,
                         duration: Long,
                         isPlaying: Boolean = false){

    Column(modifier = Modifier.fillMaxSize().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {


        /*
        AsyncImage(
            model = R.drawable.ic_launcher_background,
            contentDescription = title,
            modifier = Modifier.size(100.dp)
        )
        */
        Image(modifier = Modifier.clip(RoundedCornerShape(20.dp)).size(300.dp),
            painter = painterResource(id = R.drawable.bdportada),
            contentDescription = "Imagen de ejemplo")

        Spacer(Modifier.padding(20.dp))


        Text(title,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 24.sp, color = Color.White),)




    }

}





@Composable
@Preview(showSystemUi = true)
fun ExpandedPlayerScreen2(){

    Column() {
        ExpandedPlayerScreen(Modifier,
            "Perfecta live music",
            "Eksa y Elmar",
            duration = 40001,
            isPlaying = false
            );
    }
};