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

    @Query("SELECT * FROM playlist ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>


    @Query("SELECT COUNT(*) FROM playlistDetalle WHERE idPlaylist = :idPlaylist")
    fun countSongs(idPlaylist: Long): Flow<Int>

    @Insert
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlist WHERE idPlaylist = :idPlaylist")
    suspend fun deletePlaylist(idPlaylist: Long)

    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlistDetalle ON songs.id = playlistDetalle.songId
        WHERE playlistDetalle.idPlaylist = :idPlaylist
        ORDER BY playlistDetalle.position
    """)
    fun getPlaylistSongs(idPlaylist: Long): Flow<List<SongEntity>>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlistDetalle WHERE idPlaylist = :idPlaylist")
    suspend fun nextPosition(idPlaylist: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(detalle: PlayListDetalle)

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

    @Query("DELETE FROM playlistDetalle WHERE idPlaylist = :idPlaylist AND songId = :songId")
    suspend fun removeSongFromPlaylist(idPlaylist: Long, songId: Long)

    @Query("UPDATE playlistDetalle SET position = :position WHERE idPlaylist = :idPlaylist AND songId = :songId")
    suspend fun updatePosition(idPlaylist: Long, songId: Long, position: Int)
}
