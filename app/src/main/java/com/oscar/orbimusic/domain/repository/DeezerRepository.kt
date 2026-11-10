package com.oscar.orbimusic.domain.repository

import com.oscar.orbimusic.domain.models.Artist
import com.oscar.orbimusic.domain.models.Track

interface DeezerRepository {
    suspend fun getTrendingArtists(limit: Int = 20): Result<List<Artist>>

    suspend fun getTopTracks(limit: Int = 10): Result<List<Track>>
}
