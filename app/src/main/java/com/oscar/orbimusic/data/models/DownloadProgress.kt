package com.oscar.orbimusic.data.models

data class DownloadProgress(
    //A que cancion pertenece este progreso. La UI lo usa para pintar el loader
    //en la fila correcta de los resultados de busqueda.
    val videoId: String,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val downloadedPath: String? = null,
    //0-100. Arranca en -1 porque yt-dlp no reporta nada hasta que empieza a
    //bajar bytes; ese -1 es el que la UI pinta como spinner indeterminado.
    val progress: Int = -1
)
