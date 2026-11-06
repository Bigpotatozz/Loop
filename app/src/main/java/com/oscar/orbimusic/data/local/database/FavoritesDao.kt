package com.oscar.orbimusic.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


//INTERFACE DONDE SE EJECUTAN LAS ACCIONES SOBRE LA TABLA DE FAVORITOS
@Dao
interface FavoritesDao {

    //Obtiene todas las canciones favoritas
    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN favorites ON songs.id = favorites.songId
        ORDER BY favorites.addedAt DESC
    """)
    fun getFavoriteSongs(): Flow<List<SongEntity>>
    //Vetrifica si una cancion es favorita
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>
    //Obtiene todas las canciones favoritas (solo ids)
    @Query("SELECT songId FROM favorites")
    fun getFavoriteIds(): Flow<List<Long>>
    //Agrega una cancion a favoritos
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoritesEntity)
    //Elimina de favoritos
    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)
}
