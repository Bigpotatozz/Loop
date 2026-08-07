package com.oscar.orbimusic.presentation.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel

@OptIn(UnstableApi::class)
@Composable
fun DownloadScreen(downloadViewModel: DownloadViewModel){

    var url by rememberSaveable { mutableStateOf("")}
    var title by rememberSaveable { mutableStateOf("")}

    Column() {
        TextField(value = url, onValueChange = { url = it});
        TextField(value = title, onValueChange = { title = it});

        Button(onClick = {
            downloadViewModel.DownloadSong(url, title);
        }) {
            Text("Descargar");
        }
    }
};