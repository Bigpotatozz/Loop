package com.oscar.orbimusic.data.repository

import com.oscar.orbimusic.data.local.database.FavoritesDao
import com.oscar.orbimusic.data.local.database.FavoritesEntity
import com.oscar.orbimusic.data.local.database.PlaylistDao
import com.oscar.orbimusic.data.local.database.PlaylistEntity
import com.oscar.orbimusic.data.local.database.PlaylistWithCount
import com.oscar.orbimusic.data.local.database.SongDao
import com.oscar.orbimusic.data.local.database.SongEntity
import com.oscar.orbimusic.domain.model.Playlist
import com.oscar.orbimusic.domain.model.PlaylistSummary
import com.oscar.orbimusic.domain.model.Song
import com.oscar.orbimusic.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SongRepositoryImpl(
    private val songDao: SongDao,
    private val favoritesDao: FavoritesDao,
    private val playlistDao: PlaylistDao
): SongRepository{

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


    // ----- Favoritos -----

    override fun getFavoriteSongs(): Flow<List<Song>> {
        return favoritesDao.getFavoriteSongs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun isFavorite(songId: Long): Flow<Boolean> {
        return favoritesDao.isFavorite(songId)
    }

    override fun getFavoriteIds(): Flow<Set<Long>> {
        return favoritesDao.getFavoriteIds().map { it.toSet() }
    }

    override suspend fun toggleFavorite(songId: Long) {
        val inserted = favoritesDao.addFavorite(FavoritesEntity(songId = songId))
        if (inserted == -1L) {
            favoritesDao.removeFavorite(songId)
        }
    }

    override suspend fun removeFavorite(songId: Long) {
        favoritesDao.removeFavorite(songId)
    }


    // ----- Playlists -----

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylistsWithCount().map { rows ->
            rows.map { it.toDomainModel() }
        }
    }

    override fun getPlaylistSummaries(): Flow<List<PlaylistSummary>> {
        return combine(
            favoritesDao.getFavoriteIds(),
            playlistDao.getAllPlaylistsWithCount()
        ) { favoriteIds, playlists ->

            buildList {
                //Favoritos va primero y siempre aparece, aunque este vacio
                add(
                    PlaylistSummary(
                        id = null,
                        name = "Favoritos",
                        songCount = favoriteIds.size
                    )
                )

                playlists.forEach { row ->
                    add(
                        PlaylistSummary(
                            id = row.playlist.idPlaylist,
                            name = row.playlist.nombre,
                            songCount = row.songCount
                        )
                    )
                }
            }
        }
    }

    override fun playlistIdsContaining(songId: Long): Flow<Set<Long>> {
        return playlistDao.playlistIdsContaining(songId).map { it.toSet() }
    }

    override fun getPlaylistSongs(idPlaylist: Long): Flow<List<Song>> {
        return playlistDao.getPlaylistSongs(idPlaylist).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun createPlaylist(name: String, description: String): Long {
        //El ViewModel pasa texto plano; armar la entidad es trabajo de esta capa
        return playlistDao.createPlaylist(
            PlaylistEntity(nombre = name, descripcion = description)
        )
    }

    override suspend fun deletePlaylist(idPlaylist: Long) {
        //El CASCADE se lleva su detalle
        playlistDao.deletePlaylist(idPlaylist)
    }

    override suspend fun addSongToPlaylist(idPlaylist: Long, songId: Long) {
        playlistDao.appendSong(idPlaylist, songId)
    }

    override suspend fun removeSongFromPlaylist(idPlaylist: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(idPlaylist, songId)
    }

    override suspend fun reorderPlaylist(idPlaylist: Long, songIdsInOrder: List<Long>) {
        playlistDao.reorder(idPlaylist, songIdsInOrder)
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

//Aqui es donde los nombres en espanol de la entidad se vuelven los del dominio
private fun PlaylistWithCount.toDomainModel() = Playlist(
    id = this.playlist.idPlaylist,
    name = this.playlist.nombre,
    description = this.playlist.descripcion,
    songCount = this.songCount,
    createdAt = this.playlist.createdAt
);
