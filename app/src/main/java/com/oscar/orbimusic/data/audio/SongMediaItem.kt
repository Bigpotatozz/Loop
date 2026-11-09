package com.oscar.orbimusic.data.audio

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.oscar.orbimusic.domain.models.Song
import java.io.File

fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
    .setUri(path)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(albumArtPath?.let { Uri.fromFile(File(it)) })
            .build()
    )
    .build()
