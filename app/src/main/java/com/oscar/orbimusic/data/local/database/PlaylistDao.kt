package com.oscar.orbimusic.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow


//INTERFACE DONDE SE EJECUTAN LAS ACCIONES SOBRE PLAYLISTS Y SU DETALLE
@Dao
interface PlaylistDao {

    //Obtiene todas las playlist registradas
    @Query("SELECT * FROM playlist ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    //Cuenta las canciones de x playlist
    @Query("SELECT COUNT(*) FROM playlistDetalle WHERE idPlaylist = :idPlaylist")
    fun countSongs(idPlaylist: Long): Flow<Int>
    //Crea una playlist nueva
    @Insert
    suspend fun createPlaylist(playlist: PlaylistEntity): Long
    //Elimina una playlist
    @Query("DELETE FROM playlist WHERE idPlaylist = :idPlaylist")
    suspend fun deletePlaylist(idPlaylist: Long)

    //Obtiene las canciones de la playlist
    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlistDetalle ON songs.id = playlistDetalle.songId
        WHERE playlistDetalle.idPlaylist = :idPlaylist
        ORDER BY playlistDetalle.position
    """)
    fun getPlaylistSongs(idPlaylist: Long): Flow<List<SongEntity>>

    //Obtiene la siguiente cancion registrada en bd en base a un indice
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlistDetalle WHERE idPlaylist = :idPlaylist")
    suspend fun nextPosition(idPlaylist: Long): Int

    //Agrega una cancion a una playlist ya existente
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(detalle: PlayListDetalle)

    //Transaccion para evitar duplicados en llamadas muy rapidas
    @Transaction
    suspend fun appendSong(idPlaylist: Long, songId: Long) {
        addSongToPlaylist(
            PlayListDetalle(
                idPlaylist = idPlaylist,
                songId = songId,
                position = nextPosition(idPlaylist)
            )
        )
    }
    //Elimina una cancion de una playlist
    @Query("DELETE FROM playlistDetalle WHERE idPlaylist = :idPlaylist AND songId = :songId")
    suspend fun removeSongFromPlaylist(idPlaylist: Long, songId: Long)

    //Actualiza la posicion de una playlist
    @Query("UPDATE playlistDetalle SET position = :position WHERE idPlaylist = :idPlaylist AND songId = :songId")
    suspend fun updatePosition(idPlaylist: Long, songId: Long, position: Int)
}
