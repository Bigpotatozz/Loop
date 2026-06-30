package com.oscar.orbimusic.data.local.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow



//INTERFACE (CONTROLLER) DONDE SE EJECUTAN TODAS LAS ACCIONES EN LA BD
@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY datedAdded DESC")
    fun getAllSongs(): Flow<List<SongEntity>> //El flow es como un socket, esta emitiendo eventos en caso de cambios


}