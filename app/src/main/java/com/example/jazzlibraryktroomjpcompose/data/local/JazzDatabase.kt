package com.example.jazzlibraryktroomjpcompose.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.jazzlibraryktroomjpcompose.data.local.converters.Converters
import com.example.jazzlibraryktroomjpcompose.data.local.db.daos.ArtistDao
import com.example.jazzlibraryktroomjpcompose.data.local.db.daos.InstrumentDao
import com.example.jazzlibraryktroomjpcompose.data.local.db.daos.QuoteDao
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.InstrumentRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.QuoteRoomEntity

@Database(
    entities = [
        ArtistRoomEntity::class,
        InstrumentRoomEntity::class,
        QuoteRoomEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class JazzDatabase : RoomDatabase() {

    abstract fun artistDao(): ArtistDao
    abstract fun instrumentDao(): InstrumentDao
    abstract fun quoteDao(): QuoteDao

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