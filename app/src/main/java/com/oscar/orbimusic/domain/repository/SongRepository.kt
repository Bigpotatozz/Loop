package com.oscar.orbimusic.domain.repository

import com.oscar.orbimusic.domain.model.Playlist
import com.oscar.orbimusic.domain.model.PlaylistGroup
import com.oscar.orbimusic.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>;

    fun getOrderedByArtist(): Flow<List<Song>>;
    fun getOrderedByName(): Flow<List<Song>>;


    // ----- Favoritos -----

    fun getFavoriteSongs(): Flow<List<Song>>;

    //Para el corazon de una sola cancion, como el del reproductor expandido
    fun isFavorite(songId: Long): Flow<Boolean>;

    //Para pintar corazones en una lista: un Set para que el "contiene" de cada
    //fila sea inmediato en vez de recorrer la lista completa
    fun getFavoriteIds(): Flow<Set<Long>>;

    //Lo que el boton necesita: marca si no estaba, desmarca si si
    suspend fun toggleFavorite(songId: Long);

    suspend fun removeFavorite(songId: Long);


    // ----- Playlists -----

    fun getAllPlaylists(): Flow<List<Playlist>>;
    
    fun getPlaylistGroups(): Flow<List<PlaylistGroup>>;

    fun getPlaylistSongs(idPlaylist: Long): Flow<List<Song>>;

    //Devuelve el id que la base le asigno, para poder navegar a ella enseguida
    suspend fun createPlaylist(name: String, description: String): Long;

    suspend fun deletePlaylist(idPlaylist: Long);

    suspend fun addSongToPlaylist(idPlaylist: Long, songId: Long);

    suspend fun removeSongFromPlaylist(idPlaylist: Long, songId: Long);

    //Recibe la lista completa ya en el orden final, no un movimiento suelto
    suspend fun reorderPlaylist(idPlaylist: Long, songIdsInOrder: List<Long>);
}
