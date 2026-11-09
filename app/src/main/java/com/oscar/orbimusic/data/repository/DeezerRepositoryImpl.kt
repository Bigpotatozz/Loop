package com.oscar.orbimusic.data.repository

import android.util.Log
import com.oscar.orbimusic.data.network.DeezerClient
import com.oscar.orbimusic.data.network.dto.ArtistDto
import com.oscar.orbimusic.domain.models.Artist
import com.oscar.orbimusic.domain.repository.DeezerRepository
import kotlin.coroutines.cancellation.CancellationException

class DeezerRepositoryImpl(
    private val deezerClient: DeezerClient
): DeezerRepository {

    override suspend fun getTrendingArtists(limit: Int): Result<List<Artist>> {
        return try {
            val response = deezerClient.getTrendingArtists(limit)
            Result.success(
                //Aqui es donde se manda a llamar la funcion privada de debajo
                response.data.orEmpty().mapNotNull { it.toDomainModel() }
            )

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Fallo al traer tendencias", e)
            Result.failure(e)
        }
    }
}


//Es una funcion de extension, basicamente es decirle que a artistDto le vas a agregar esta funcion en su clase
//Por eso es que arriba se manda a llamar con un it
private fun ArtistDto.toDomainModel(): Artist? {
    val safeId = id ?: return null
    val safeName = name?.takeIf { it.isNotBlank() } ?: return null
    val xl = pictureXl ?: pictureBig ?: pictureMedium ?: return null
    val medium = pictureMedium ?: pictureBig ?: pictureXl ?: return null

    return Artist(
        id = safeId,
        name = safeName,
        pictureMedium = medium,
        pictureXl = xl,
        fanCount = nbFan,
        albumCount = nbAlbum,
        position = position
    )
}
