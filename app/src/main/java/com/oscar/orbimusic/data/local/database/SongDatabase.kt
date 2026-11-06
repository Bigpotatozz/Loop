package com.oscar.orbimusic.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [
        SongEntity::class,
        FavoritesEntity::class,
        PlaylistEntity::class,
        PlayListDetalle::class,
        PlayHistoryEntity::class
    ],
    version = 3,
    exportSchema = false //Si esta en true guarda en un archivo el schema
)
abstract class SongDatabase: RoomDatabase() {

    //Retorna el Dao
    abstract fun songDao(): SongDao;
    abstract fun favoritesDao(): FavoritesDao;
    abstract fun playlistDao(): PlaylistDao;
    abstract fun playHistoryDao(): PlayHistoryDao;

    //En un companion object se puede meter codigo estatico para no crear una instancia
    companion object {
        // @Volatile = le dice a Kotlin que esta variable puede cambiar
        // desde múltiples threads, así evita problemas de concurrencia
        @Volatile
        private var INSTANCE: SongDatabase? = null; //Instancia de la bd

        //Funcion que retorna dicha
        fun getInstance(context: Context): SongDatabase{
            //Si no existe la bd sincroniza una
            return INSTANCE ?: synchronized(this){
                //crea la bd
                Room.databaseBuilder(
                    context.applicationContext, //Contexto de la aplicacion
                    SongDatabase::class.java, //Clase de la base de datos
                    "song_database" //Nombre del archivo sqlite que se crea
                )
                    .fallbackToDestructiveMigration() // Si se cambia de version se borra la vieja y se crea la nueva
                    .build() // Construye la bd
                    .also {INSTANCE = it} //Guarda la bd en la instancia
            }
        }


    }






}