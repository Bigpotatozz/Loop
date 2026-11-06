package com.oscar.orbimusic.data.local.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface PlayHistoryDao {

    @Insert
    suspend fun record(play: PlayHistoryEntity)
}
