package com.oscar.orbimusic.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.oscar.orbimusic.data.audio.FileScanner
import com.oscar.orbimusic.data.local.database.SongDao
import com.oscar.orbimusic.data.local.database.SongDatabase
import com.oscar.orbimusic.data.repository.SongRepositoryImpl
import com.oscar.orbimusic.domain.repository.SongRepository
import com.oscar.orbimusic.presentation.viewmodel.DownloadViewModel
import com.oscar.orbimusic.presentation.viewmodel.MediaViewModel

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
    private val songRepository: SongRepository by lazy {
        SongRepositoryImpl(songDao);
    };


    suspend fun scanMusicFiles(context: Context){
        val fileScanner = FileScanner(context, songDao);
        fileScanner.scanMusicFolder(context)
    }

    fun provideMediaViewModel(): MediaViewModel {
        return MediaViewModel(songRepository, context);
    }

    @OptIn(UnstableApi::class)
    fun provideDownloadViewModel(): DownloadViewModel{
        return DownloadViewModel(context)
    }



}