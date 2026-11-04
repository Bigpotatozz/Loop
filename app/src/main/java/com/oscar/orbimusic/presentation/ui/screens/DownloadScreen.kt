package com.oscar.orbimusic.presentation.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel

@OptIn(UnstableApi::class)
@Composable
fun DownloadScreen(downloadViewModel: DownloadViewModel){

    var title by rememberSaveable { mutableStateOf("")}

    Column() {

        Column() {
            Text(text = "Buscar", style = TextStyle(fontSize = 20.sp))
        }


        OutlinedTextField(value = title, onValueChange = { title = it}, modifier = Modifier.fillMaxWidth());
    }
};




@Composable
fun WidgetSuggestion(label: String, url: String){

    Column(Modifier.clickable()) {
        Text(label)
    }


};