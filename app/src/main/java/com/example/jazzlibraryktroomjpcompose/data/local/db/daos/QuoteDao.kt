package com.example.jazzlibraryktroomjpcompose.data.local.db.daos


import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.QuoteRoomEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes ORDER BY quote_id DESC")
    fun getAllQuotes(): Flow<List<QuoteRoomEntity>>

    @Query("SELECT * FROM quotes WHERE artist_id = :artistId")
    fun getQuotesByArtist(artistId: Int): Flow<List<QuoteRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuotes(quotes: List<QuoteRoomEntity>)

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()
}