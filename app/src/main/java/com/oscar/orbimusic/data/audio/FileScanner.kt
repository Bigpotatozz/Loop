package com.oscar.orbimusic.data.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import com.oscar.orbimusic.data.local.database.SongDao
import com.oscar.orbimusic.data.local.database.SongEntity
import java.io.File

class FileScanner(
    private val context: Context,
    private val songDao: SongDao
) {


    suspend fun scanMusicFolder() {
        Log.d("FileScanner", "Iniciando escaneo de música...")

        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        Log.d("FileScanner", "Buscando en: ${musicDir.absolutePath}")

        if (!musicDir.exists()) {
            Log.e("FileScanner", "La carpeta de música no existe")
            return
        }

        scanDirectoryRecursive(musicDir)
        Log.d("FileScanner", "Escaneo finalizado")
    }

    private suspend fun scanDirectoryRecursive(directory: File) {
        val files = directory.listFiles() ?: return

        files.forEach { file ->
            if (file.isDirectory) {
                scanDirectoryRecursive(file)
            } else if (file.extension.lowercase() == "mp3") {
                val song = extractMetaData(file)
                if (song != null) {
                    if(song.title != "Desconocido"){
                        songDao.postSong(song)
                        Log.d("FileScanner", "Canción insertada: ${song.title} desde ${file.name}")
                    }
                }
            }
        }
    }


    //Extrae la metadata del archivo
    private fun extractMetaData(file: File): SongEntity?{

        return try{

            //Clase de android que extrae metadatos
            val retriever = MediaMetadataRetriever();
            //Le indicas la ruta del archivo
            retriever.setDataSource(file.path);

            //Extrae los datos
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: "Desconocido"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Desconocido"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Desconocido"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            //Cierra el archivo
            retriever.release();

            //crea la entidad con los datos extraidos
            SongEntity(
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                path = file.path);


        }catch (e: Exception){
            null
        }

    }

}