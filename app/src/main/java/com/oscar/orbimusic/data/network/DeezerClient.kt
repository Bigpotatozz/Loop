package com.oscar.orbimusic.data.network

import com.oscar.orbimusic.data.network.dto.ArtistDto
import com.oscar.orbimusic.data.network.dto.DeezerListDto
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerClient {

    @GET("chart/0/artists")
    suspend fun getTrendingArtists(
        @Query("limit") limit: Int = 20
    ): DeezerListDto<ArtistDto>

}