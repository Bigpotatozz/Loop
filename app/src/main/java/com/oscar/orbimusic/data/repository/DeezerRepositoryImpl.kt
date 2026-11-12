package com.oscar.orbimusic.data.repository

import android.util.Log
import com.oscar.orbimusic.data.network.DeezerClient
import com.oscar.orbimusic.data.network.dto.AlbumDto
import com.oscar.orbimusic.data.network.dto.ArtistDto
import com.oscar.orbimusic.data.network.dto.TrackDto
import com.oscar.orbimusic.domain.models.Album
import com.oscar.orbimusic.domain.models.AlbumDetail
import com.oscar.orbimusic.domain.models.Artist
import com.oscar.orbimusic.domain.models.Track
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

    override suspend fun getTopTracks(limit: Int): Result<List<Track>> {
        return try {
            val response = deezerClient.getTopTracks(limit)
            Result.success(
                response.data.orEmpty().mapNotNull { it.toDomainModel() }
            )

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Fallo al traer el top de canciones", e)
            Result.failure(e)
        }
    }

    override suspend fun getTopAlbums(limit: Int): Result<List<Album>> {
        return try {
            val response = deezerClient.getTopAlbums(limit)
            Result.success(
                response.data.orEmpty().mapNotNull { it.toDomainModel() }
            )

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Fallo al traer el top de albumes", e)
            Result.failure(e)
        }
    }

    override suspend fun getAlbum(id: Long): Result<AlbumDetail> {
        return try {
            val response = deezerClient.getAlbum(id)
            //Null = Deezer contesto {"error":...} o falto algo indispensable
            val album = response.toAlbumDetail()
                
                ?: return Result.failure(IllegalStateException("Álbum no encontrado"))
            Result.success(album)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Fallo al traer el album $id", e)
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


private fun TrackDto.toDomainModel(): Track? {
    val safeId = id ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val safeArtist = artist?.name?.takeIf { it.isNotBlank() } ?: return null

    val medium = album?.coverMedium ?: album?.coverXl ?: return null
    val xl = album?.coverXl ?: album?.coverMedium ?: return null

    return Track(
        id = safeId,
        title = safeTitle,
        artistName = safeArtist,
        coverMedium = medium,
        coverXl = xl,
        durationSeconds = duration,
        position = position,
        previewUrl = preview?.takeIf { it.startsWith("http") }
    )
}


//Mismas guardas. Aqui la caratula viene en el propio album, no anidada.
private fun AlbumDto.toDomainModel(): Album? {
    val safeId = id ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val safeArtist = artist?.name?.takeIf { it.isNotBlank() } ?: return null

    val medium = coverMedium ?: coverXl ?: return null
    val xl = coverXl ?: coverMedium ?: return null

    return Album(
        id = safeId,
        title = safeTitle,
        artistName = safeArtist,
        coverMedium = medium,
        coverXl = xl,
        position = position
    )
}


//Mismas guardas que el de arriba, pero con las canciones adentro.
private fun AlbumDto.toAlbumDetail(): AlbumDetail? {
    val safeId = id ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val safeArtist = artist?.name?.takeIf { it.isNotBlank() } ?: return null
    val xl = coverXl ?: coverMedium ?: return null
    val medium = coverMedium ?: coverXl ?: return null

    return AlbumDetail(
        id = safeId,
        title = safeTitle,
        artistName = safeArtist,
        coverXl = xl,
        releaseDate = releaseDate,
        recordType = recordType,
        durationSeconds = duration,
        tracks = tracks?.data.orEmpty().mapIndexedNotNull { index, track ->
            track.toAlbumTrack(
                albumArtist = safeArtist,
                coverMedium = medium,
                coverXl = xl,
                position = index + 1
            )
        }
    )
}


//Dentro de album/{id} las canciones no traen "album", asi que la caratula se
//hereda del album. El artista si viene, pero si faltara se usa el del album.
private fun TrackDto.toAlbumTrack(
    albumArtist: String,
    coverMedium: String,
    coverXl: String,
    position: Int
): Track? {
    val safeId = id ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null

    return Track(
        id = safeId,
        title = safeTitle,
        artistName = artist?.name?.takeIf { it.isNotBlank() } ?: albumArtist,
        coverMedium = coverMedium,
        coverXl = coverXl,
        durationSeconds = duration,
        position = position,
        previewUrl = preview?.takeIf { it.startsWith("http") }
    )
}
