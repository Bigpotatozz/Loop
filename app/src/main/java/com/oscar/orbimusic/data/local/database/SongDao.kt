package com.oscar.orbimusic.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oscar.orbimusic.domain.model.Song
import kotlinx.coroutines.flow.Flow



//INTERFACE (CONTROLLER) DONDE SE EJECUTAN TODAS LAS ACCIONES EN LA BD
@Dao
interface SongDao {

    //Obtiene todas las canciones
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getAllSongs(): Flow<List<SongEntity>> //El flow es como un socket, esta emitiendo eventos en caso de cambios

    //crea una nueva cancion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun postSong(song: SongEntity)

    //Busca por titulo de cancion y artista
    @Query("SELECT * FROM songs WHERE title = :title AND artist = :artist")
    suspend fun searchByTitleAndArtist(title: String, artist: String): SongEntity?
    //busca por path(supongo que por ruta de archivo)
    @Query("SELECT * FROM songs WHERE path = :path")
    suspend fun searchByPath(path: String): SongEntity?
    //Actualiza la caratula ruta de la caratula de la cancion
    @Query("UPDATE songs SET albumArtPath = :albumArtPath WHERE id = :id")
    suspend fun updateAlbumArt(id: Long, albumArtPath: String?)
    //Obtiene las canciones y ordena por artista
    @Query("SELECT * FROM songs ORDER BY artist")
    fun getOrderedByArtist(): Flow<List<SongEntity>>;
    //Obtiene las canciones y ordena por titulo
    @Query("SELECT * FROM songs ORDER BY title")
    fun getOrderedByName(): Flow<List<SongEntity>>;
    //Obtiene una cancion aleatoria
    @Query("SELECT * FROM songs ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomSong(): SongEntity?

    //Borra una cancion
    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)



}