package com.oscar.orbimusic.data.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import com.oscar.orbimusic.data.local.database.SongDao
import com.oscar.orbimusic.data.local.database.SongEntity
import java.io.File

class FileScanner(
    private val context: Context,
    private val songDao: SongDao
) {


    suspend fun scanMusicFolder(){

        //Accede a la carpeta de musica del dispositivo
        val musicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path
        );

        //Si no existe retorna
        if(!musicDir.exists()){
            return;
        }
        //Lista los archivos con extension mp3
        val mp3Files = musicDir.listFiles {file ->
            file.extension == "mp3";

        } ?: emptyArray();

        //Recorre cada archivo y lo va insertando en la bd de sqlite
        mp3Files.forEach { file ->
            val song = extractMetaData(file);
            if(song != null){
                songDao.postSong(song);
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