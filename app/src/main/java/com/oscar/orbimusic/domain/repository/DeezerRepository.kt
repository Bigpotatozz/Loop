package com.oscar.orbimusic.domain.repository

import com.oscar.orbimusic.domain.models.Artist

interface DeezerRepository {
    suspend fun getTrendingArtists(limit: Int = 20): Result<List<Artist>>
}
