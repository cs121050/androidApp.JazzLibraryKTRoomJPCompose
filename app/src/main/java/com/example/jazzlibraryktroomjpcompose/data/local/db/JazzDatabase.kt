package com.example.jazzlibraryktroomjpcompose.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.jazzlibraryktroomjpcompose.data.local.db.daos.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.*

@Database(
    entities = [
        ArtistRoomEntity::class,
        QuoteRoomEntity::class,
        InstrumentRoomEntity::class,
        TypeRoomEntity::class,
        DurationRoomEntity::class,
        VideoRoomEntity::class,
        VideoContainsArtistRoomEntity::class,
        FilterPathRoomEntity::class,
        FilterPathContainsMediaRoomEntity::class,
        SongRoomEntity::class,
        AlbumRoomEntity::class,
        AlbumContainsArtistRoomEntity::class,
        SearchHistoryRoomEntity::class
    ],
    version = 17,
    exportSchema = true //gives json for fun
)
abstract class JazzDatabase : RoomDatabase() {

    abstract fun artistDao(): ArtistDao
    abstract fun instrumentDao(): InstrumentDao
    abstract fun quoteDao(): QuoteDao
    abstract fun typeDao(): TypeDao
    abstract fun durationDao(): DurationDao
    abstract fun videoDao(): VideoDao
    abstract fun videoContainsArtistDao(): VideoContainsArtistDao
    abstract fun filterPathDao(): FilterPathDao
    abstract fun filterPathContainsMediaDao(): FilterPathContainsMediaDao
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun albumContainsArtistDao(): AlbumContainsArtistDao
    abstract fun searchHistoryDao(): SearchHistoryDao



    companion object {
        @Volatile  //Do not cache this variable in threads. Whenever a thread reads INSTANCE, read it straight from main memory.
        private var INSTANCE: JazzDatabase? = null

        fun getDatabase(context: Context): JazzDatabase {
            return INSTANCE ?: synchronized(JazzDatabase::class.java) { //ensure that only one instance of db is ever created,  This is the lock. If 100 threads call getDatabase at the exact same millisecond, only one thread is allowed past the lock. The other 99 wait in line.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JazzDatabase::class.java,
                    "jazz_library.db"
                )
                    // For simplicity during development, it will clean all the db, instead do -> addMigrations(
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}