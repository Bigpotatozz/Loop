package com.oscar.orbimusic.data.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import com.oscar.orbimusic.data.local.database.SongDao
import com.oscar.orbimusic.data.local.database.SongEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

class FileScanner(
    private val context: Context,
    private val songDao: SongDao
) {


    suspend fun scanMusicFolder(context: Context) {

        val dir = File(
            context.getExternalFilesDir(null),
            "Music"
        )

        Log.d("FileScanner", "Escaneando carpeta: ${dir.absolutePath}")
        Log.d("FileScanner", "Carpeta existe: ${dir.exists()}")

        if (!dir.exists()) {
            dir.mkdirs()
        }
        scanDirectoryRecursive(dir);
    }



    private suspend fun scanDirectoryRecursive(directory: File) {
        val files = directory.listFiles() ?: return
        files.forEach { file ->

            if (file.isDirectory) {
                scanDirectoryRecursive(file)
            } else if (file.extension.lowercase() in listOf("mp3", "m4a", "wav", "flac")) {
                val song = extractMetaData(file)
                if (song != null) {

                        val repeated = songDao.searchByPath(song.path);

                        if(repeated != null){
                            Log.d("FileScanner", "Cancion repetida, omitiendo");
                        }else{
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
            var albumArthPath: String? = null;

            val picture = retriever.embeddedPicture;

            if(picture != null){
                albumArthPath = savePictureToFile(picture,  file.nameWithoutExtension)

            }

            retriever.release();

            //crea la entidad con los datos extraidos
            SongEntity(
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                albumArtPath = albumArthPath,
                path = file.path);


        }catch (e: Exception){
            null
        }

    }


    fun savePictureToFile(picture: ByteArray, songName: String): String?{
        return try{

            val albumArtDir = File(context.cacheDir, "album_art");
            if (!albumArtDir.exists()) {
                albumArtDir.mkdirs()
            }

            val pictureFile = File(albumArtDir, "$songName.jpg");

            FileOutputStream(pictureFile).use { output ->
                output.write(picture)
            }

            pictureFile.path

        }catch (e: Exception) {
            null
        }


    }

}