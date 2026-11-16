package com.oscar.orbimusic.data.repository

import android.util.Log
import com.oscar.orbimusic.data.network.DeezerClient
import com.oscar.orbimusic.data.network.dto.AlbumDto
import com.oscar.orbimusic.data.network.dto.ArtistDto
import com.oscar.orbimusic.data.network.dto.TrackDto
import com.oscar.orbimusic.domain.models.Album
import com.oscar.orbimusic.domain.models.AlbumDetail
import com.oscar.orbimusic.domain.models.Artist
import com.oscar.orbimusic.domain.models.ArtistDetail
import com.oscar.orbimusic.domain.models.DeezerSearchResults
import com.oscar.orbimusic.domain.models.Track
import com.oscar.orbimusic.domain.repository.DeezerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.cancellation.CancellationException

//Artist no es data class, asi que no tiene copy()
private fun Artist.withFanCount(fans: Int?): Artist = Artist(
    id = id,
    name = name,
    pictureMedium = pictureMedium,
    pictureXl = pictureXl,
    fanCount = fans ?: fanCount,
    albumCount = albumCount,
    position = position
)

class DeezerRepositoryImpl(
    private val deezerClient: DeezerClient
): DeezerRepository {

    override suspend fun getTrendingArtists(limit: Int): Result<List<Artist>> {
        return try {
            val response = deezerClient.getTrendingArtists(limit)
            //Aqui es donde se manda a llamar la funcion privada de debajo
            val artists = response.data.orEmpty().mapNotNull { it.toDomainModel() }.distinctBy { it.id } // ids repetidos tronarian las keys del LazyRow

            //El chart no trae nb_fan, solo artist/{id}. Se piden todos en paralelo 
            val withFans = coroutineScope {
                artists.map { artist ->
                    async {
                        val fans = try {
                            deezerClient.getArtist(artist.id).nbFan
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.w("DeezerRepository", "Sin fans para ${artist.name}", e)
                            null
                        }
                        artist.withFanCount(fans)
                    }
                }.awaitAll()
            }

            Result.success(withFans)

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
                response.data.orEmpty().mapNotNull { it.toDomainModel() }.distinctBy { it.id } // ids repetidos tronarian las keys del LazyRow
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
                response.data.orEmpty().mapNotNull { it.toDomainModel() }.distinctBy { it.id } // ids repetidos tronarian las keys del LazyRow
            )

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Fallo al traer el top de albumes", e)
            Result.failure(e)
        }
    }

    override suspend fun getChartCountry(): Result<String> {
        return try {
            val country = deezerClient.getInfos().country?.takeIf { it.isNotBlank() }
                ?: return Result.failure(IllegalStateException("Deezer no reportó país"))
            Result.success(country)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Fallo al traer el pais", e)
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

    override suspend fun search(query: String, limit: Int): Result<DeezerSearchResults> {
        return try {
            coroutineScope {
                val artists = async {
                    searchPart("artistas", query) {
                        deezerClient.searchArtists(query, limit).data.orEmpty()
                            .mapNotNull { it.toDomainModel() }
                    }
                }
                val albums = async {
                    searchPart("albumes", query) {
                        deezerClient.searchAlbums(query, limit).data.orEmpty()
                            .mapNotNull { it.toDomainModel() }
                    }
                }
                val tracks = async {
                    searchPart("canciones", query) {
                        //Se numeran por relevancia: la fila del album pinta
                        //"position" y la busqueda no la trae
                        deezerClient.searchTracks(query, limit).data.orEmpty()
                            .mapIndexedNotNull { index, track -> track.toDomainModel(positionOverride = index + 1) }
                    }
                }

                val a = artists.await()
                val b = albums.await()
                val c = tracks.await()

                if (a == null && b == null && c == null) {
                    Result.failure(IllegalStateException("No se pudo buscar. Revisa tu conexión."))
                } else {
                    Result.success(
                        DeezerSearchResults(
                            artists = a.orEmpty().distinctBy { it.id },
                            albums = b.orEmpty().distinctBy { it.id },
                            tracks = c.orEmpty().distinctBy { it.id }
                        )
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Fallo la busqueda \"$query\"", e)
            Result.failure(e)
        }
    }

    private suspend fun <T> searchPart(label: String, query: String, block: suspend () -> List<T>): List<T>? {
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("DeezerRepository", "Fallo la busqueda de $label \"$query\"", e)
            null
        }
    }

    override suspend fun getArtist(id: Long, topLimit: Int): Result<ArtistDetail> {
        return try {
            val artist = coroutineScope {
                //Las tres peticiones son independientes: van en paralelo
                val detail = async { deezerClient.getArtist(id) }

                //Igual que el top: si falla, el carrusel simplemente no sale
                val albums = async {
                    try {
                        deezerClient.getArtistAlbums(id).data.orEmpty()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w("DeezerRepository", "Sin albumes para el artista $id", e)
                        emptyList()
                    }
                }

                val related = async {
                    try {
                        deezerClient.getRelatedArtists(id).data.orEmpty()
                            .mapNotNull { it.toDomainModel() }
                            .distinctBy { it.id } // ids repetidos tronarian las keys del LazyRow
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w("DeezerRepository", "Sin similares para el artista $id", e)
                        emptyList()
                    }
                }

                //Si falla el top la pantalla se muestra igual con la lista
                //vacia; solo el detalle es indispensable
                val top = async {
                    try {
                        deezerClient.getArtistTop(id, topLimit).data.orEmpty()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w("DeezerRepository", "Sin top para el artista $id", e)
                        emptyList()
                    }
                }

                //El orden de /top es el ranking; se numera desde 1
                val tracks = top.await()
                    .mapIndexedNotNull { index, track -> track.toDomainModel(positionOverride = index + 1) }
                    .distinctBy { it.id } // ids repetidos tronarian las keys del LazyColumn

                detail.await().toArtistDetail(tracks, albums.await(), related.await())
            }

            //Null = Deezer contesto 200 con {"error":...}, igual que album/{id}
                ?: return Result.failure(IllegalStateException("Artista no encontrado"))
            Result.success(artist)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Fallo al traer el artista $id", e)
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


//positionOverride: artist/{id}/top no manda "position", se numera por indice
private fun TrackDto.toDomainModel(positionOverride: Int? = null): Track? {
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
        position = positionOverride ?: position,
        previewUrl = preview?.takeIf { it.startsWith("http") }
    )
}


//Mismas guardas que toDomainModel: sin id, nombre o foto no hay pantalla
private fun ArtistDto.toArtistDetail(
    topTracks: List<Track>,
    albumDtos: List<AlbumDto>,
    related: List<Artist>
): ArtistDetail? {
    val safeId = id ?: return null
    val safeName = name?.takeIf { it.isNotBlank() } ?: return null
    val xl = pictureXl ?: pictureBig ?: pictureMedium ?: return null

    return ArtistDetail(
        id = safeId,
        name = safeName,
        pictureXl = xl,
        fanCount = nbFan,
        albumCount = nbAlbum,
        topTracks = topTracks,
        albums = albumDtos.toLatestAlbums(safeName),
        //Por si Deezer incluyera al propio artista entre sus similares
        related = related.filter { it.id != safeId }
    )
}


//Cuantos entran en el carrusel de la pantalla de artista
private const val ARTIST_ALBUMS_LIMIT = 10

//Solo albumes y EPs: los sencillos y recopilaciones enterrarian los albumes
//de artistas que sacan muchos sencillos
private val ARTIST_ALBUM_TYPES = setOf("album", "ep")

private fun List<AlbumDto>.toLatestAlbums(artistName: String): List<Album> =
    filter { it.recordType in ARTIST_ALBUM_TYPES }
        .mapNotNull { it.toArtistAlbum(artistName) }
        .distinctBy { it.id } // ids repetidos tronarian las keys del LazyRow
        .sortedByDescending { it.releaseDate.orEmpty() }
        .take(ARTIST_ALBUMS_LIMIT)


private fun AlbumDto.toArtistAlbum(artistName: String): Album? {
    val safeId = id ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val medium = coverMedium ?: coverXl ?: return null
    val xl = coverXl ?: coverMedium ?: return null

    return Album(
        id = safeId,
        title = safeTitle,
        artistName = artist?.name?.takeIf { it.isNotBlank() } ?: artistName,
        coverMedium = medium,
        coverXl = xl,
        releaseDate = releaseDate,
        recordType = recordType
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
