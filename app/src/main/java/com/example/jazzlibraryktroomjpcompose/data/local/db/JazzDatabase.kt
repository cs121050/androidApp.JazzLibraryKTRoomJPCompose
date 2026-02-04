package com.example.jazzlibraryktroomjpcompose.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.jazzlibraryktroomjpcompose.data.local.converters.Converters
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
        FilterPathRoomEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class JazzDatabase : RoomDatabase() {

    abstract fun artistDao(): ArtistDao
    abstract fun instrumentDao(): InstrumentDao
    abstract fun quoteDao(): QuoteDao
    abstract fun typeDao(): TypeDao
    abstract fun durationDao(): DurationDao
    abstract fun videoDao(): VideoDao
    abstract fun videoContainsArtistDao(): VideoContainsArtistDao
    abstract fun filterPathDao(): FilterPathDao


    companion object {
        @Volatile
        private var INSTANCE: JazzDatabase? = null

        fun getDatabase(context: Context): JazzDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JazzDatabase::class.java,
                    "jazz_library.db"
                )
                    .fallbackToDestructiveMigration() // For simplicity during development
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}