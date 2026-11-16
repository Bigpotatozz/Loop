package com.oscar.orbimusic.domain.repository

import com.oscar.orbimusic.domain.models.Album
import com.oscar.orbimusic.domain.models.AlbumDetail
import com.oscar.orbimusic.domain.models.Artist
import com.oscar.orbimusic.domain.models.ArtistDetail
import com.oscar.orbimusic.domain.models.DeezerSearchResults
import com.oscar.orbimusic.domain.models.Track

interface DeezerRepository {
    suspend fun getTrendingArtists(limit: Int = 20): Result<List<Artist>>

    suspend fun getTopTracks(limit: Int = 10): Result<List<Track>>

    suspend fun getTopAlbums(limit: Int = 10): Result<List<Album>>

    suspend fun getAlbum(id: Long): Result<AlbumDetail>

    //Artistas, albumes y canciones a la vez, para el buscador del Home
    suspend fun search(query: String, limit: Int = 10): Result<DeezerSearchResults>

    //Detalle del artista (fans, albumes) + sus canciones populares
    suspend fun getArtist(id: Long, topLimit: Int = 5): Result<ArtistDetail>

    //Nombre del pais de los charts ("México")
    suspend fun getChartCountry(): Result<String>
}
