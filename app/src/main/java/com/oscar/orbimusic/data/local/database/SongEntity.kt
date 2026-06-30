package com.oscar.orbimusic.data.local.database

import androidx.room.Entity
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
) {





}