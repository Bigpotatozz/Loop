package com.oscar.orbimusic.domain.repository

import com.oscar.orbimusic.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>;

    fun getOrderedByArtist(): Flow<List<Song>>;
    fun getOrderedByName(): Flow<List<Song>>;

}