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

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getAllSongs(): Flow<List<SongEntity>> //El flow es como un socket, esta emitiendo eventos en caso de cambios

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun postSong(song: SongEntity)


    @Query("SELECT * FROM songs WHERE title = :title AND artist = :artist")
    suspend fun searchByTitleAndArtist(title: String, artist: String): SongEntity?

    @Query("SELECT * FROM songs WHERE path = :path")
    suspend fun searchByPath(path: String): SongEntity?



}