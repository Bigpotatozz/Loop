package com.oscar.orbimusic.data.network

import com.oscar.orbimusic.data.network.dto.AlbumDto
import com.oscar.orbimusic.data.network.dto.ArtistDto
import com.oscar.orbimusic.data.network.dto.DeezerListDto
import com.oscar.orbimusic.data.network.dto.TrackDto
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerClient {

    @GET("chart/0/artists")
    suspend fun getTrendingArtists(
        @Query("limit") limit: Int = 20
    ): DeezerListDto<ArtistDto>

    //El top de canciones por pais, deezer agarra el pais por la ip del usuario
    @GET("chart/0/tracks")
    suspend fun getTopTracks(
        @Query("limit") limit: Int = 10
    ): DeezerListDto<TrackDto>

    //El top de albumes. Mismo criterio de pais por IP que el de canciones.
    @GET("chart/0/albums")
    suspend fun getTopAlbums(
        @Query("limit") limit: Int = 10
    ): DeezerListDto<AlbumDto>

    //El detalle de un artista. Se usa por nb_fan: chart/0/artists no lo trae.
    @GET("artist/{id}")
    suspend fun getArtist(
        @Path("id") id: Long
    ): ArtistDto

    //El album completo con sus canciones en "tracks". Ojo: si el id no existe
    //Deezer responde 200 con {"error":{...}}, asi que llega un AlbumDto vacio.
    @GET("album/{id}")
    suspend fun getAlbum(
        @Path("id") id: Long
    ): AlbumDto

}