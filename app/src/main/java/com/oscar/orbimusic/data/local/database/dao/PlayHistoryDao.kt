package com.oscar.orbimusic.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.oscar.orbimusic.data.local.database.entities.PlayHistoryEntity

@Dao
interface PlayHistoryDao {

    @Insert
    suspend fun record(play: PlayHistoryEntity)
}
