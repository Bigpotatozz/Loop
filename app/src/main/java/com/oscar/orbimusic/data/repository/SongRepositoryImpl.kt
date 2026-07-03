package com.oscar.orbimusic.data.repository

import com.oscar.orbimusic.data.local.database.SongDao
import com.oscar.orbimusic.data.local.database.SongEntity
import com.oscar.orbimusic.domain.model.Song
import com.oscar.orbimusic.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl(private val songDao: SongDao): SongRepository{

    override fun getAllSongs(): Flow<List<Song>> {
        //Recorre la lista que da el DAO <SongEntity>
        return songDao.getAllSongs().map { entities ->
            //Recorre la lista de SongEntity y los va parseando a Song
            entities.map { it.toDomainModel()}
        }


    }

    override fun getOrderedByName(): Flow<List<Song>> {
        return songDao.getOrderedByName().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getOrderedByArtist(): Flow<List<Song>> {
        return songDao.getOrderedByArtist().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }


}

private fun SongEntity.toDomainModel() = Song(
    id = this.id,
    title = this.title,
    artist = this.artist,
    album = this.album,
    duration = this.duration,
    path = this.path,
    albumArtPath = this.albumArtPath,
);