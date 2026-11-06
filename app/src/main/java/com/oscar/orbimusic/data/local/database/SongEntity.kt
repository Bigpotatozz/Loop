package com.oscar.orbimusic.data.local.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


//ENTIDAD DE SQLITE FUNCIONA COMO UN MODELO O SCHEMA CUALQUIERA
@Entity("songs")
data class SongEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
) {};

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],  //columna en songs
            childColumns = ["songId"], //columna en esta tabla
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoritesEntity(
    @PrimaryKey
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
){};

@Entity(
    tableName = "playlist"
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val idPlaylist: Long = 0,
    val nombre: String,
    val descripcion: String,
    val createdAt: Long = System.currentTimeMillis()
){};


@Entity(tableName = "playlistDetalle",
    primaryKeys = ["idPlaylist", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["idPlaylist"],
            childColumns = ["idPlaylist"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
   
    indices = [Index("songId")]
)
data class PlayListDetalle(
    //Las columnas hijas de las llaves foraneas tienen que existir como campos
    val idPlaylist: Long,
    val songId: Long,
    //Una playlist tiene orden; sin esto no hay donde guardarlo
    val position: Int
){}