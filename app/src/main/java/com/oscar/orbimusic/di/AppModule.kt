package com.oscar.orbimusic.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.data.audio.FileScanner
import com.oscar.orbimusic.data.local.database.dao.FavoritesDao
import com.oscar.orbimusic.data.local.database.dao.PlayHistoryDao
import com.oscar.orbimusic.data.local.database.dao.PlaylistDao
import com.oscar.orbimusic.data.local.database.dao.SongDao
import com.oscar.orbimusic.data.local.database.SongDatabase
import com.oscar.orbimusic.data.network.DeezerClient
import com.oscar.orbimusic.data.repository.DeezerRepositoryImpl
import com.oscar.orbimusic.data.repository.SongRepositoryImpl
import com.oscar.orbimusic.domain.repository.DeezerRepository
import com.oscar.orbimusic.domain.repository.SongRepository
import com.oscar.orbimusic.presentation.viewmodel.AlbumScreenViewModel
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel
import com.oscar.orbimusic.presentation.viewmodel.HomeViewModel
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppModule {

    //Accede al contexto
    private lateinit var context: Context;

    fun initialize(appContext: Context){
        context = appContext.applicationContext;
    }
    private val songDatabaseInit: SongDatabase by lazy {
        SongDatabase.getInstance(context);
    };
    private val songDao: SongDao by lazy {
        songDatabaseInit.songDao();
    };
    private val favoritesDao: FavoritesDao by lazy {
        songDatabaseInit.favoritesDao();
    };
    private val playlistDao: PlaylistDao by lazy {
        songDatabaseInit.playlistDao();
    };
    private val playHistoryDao: PlayHistoryDao by lazy {
        songDatabaseInit.playHistoryDao();
    };
    private val songRepository: SongRepository by lazy {
        SongRepositoryImpl(songDao, favoritesDao, playlistDao, playHistoryDao);
    };


    suspend fun scanMusicFiles(context: Context){
        val fileScanner = FileScanner(context, songDao);
        fileScanner.scanMusicFolder(context)
    }

    val mediaViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { MediaViewModel(songRepository, context) }
    }

    @OptIn(UnstableApi::class)
    val downloadViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { DownloadViewModel(context) }
    }

    //No recibe context: Retrofit no lo necesita, a diferencia de los otros dos
    val homeViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { HomeViewModel(deezerRepository) }
    }

    //Se crea por pantalla (owner = NavBackStackEntry), no por Activity: cada
    //album abierto tiene su propio estado y se libera al hacer pop
    val albumViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { AlbumScreenViewModel(deezerRepository) }
    }

    private val deezerApi: DeezerClient by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeezerClient::class.java)
    }

    private val deezerRepository: DeezerRepository by lazy {
        DeezerRepositoryImpl(deezerApi);
    };


}