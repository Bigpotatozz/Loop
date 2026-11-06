package com.oscar.orbimusic.data.local.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("songId"), Index("playedAt")]
)
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val songId: Long,

    val playedAt: Long = System.currentTimeMillis(),
    val msPlayed: Long,
    val shuffle: Boolean,
    val source: String
)

object PlaySource {
    const val LIBRARY = "biblioteca"
    const val FAVORITES = "favoritos"

    fun playlist(idPlaylist: Long) = "playlist:$idPlaylist"
}
