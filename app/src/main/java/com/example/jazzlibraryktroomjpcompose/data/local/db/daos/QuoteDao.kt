package com.example.jazzlibraryktroomjpcompose.data.local.db.daos


import androidx.room.*
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.ArtistRoomEntity
import com.example.jazzlibraryktroomjpcompose.data.local.db.entities.QuoteRoomEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes ORDER BY quote_id DESC")
    fun getAllQuotes(): Flow<List<QuoteRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuotes(quotes: List<QuoteRoomEntity>)

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()

    @Query("DELETE FROM quotes where quote_id =  :quoteId")
    suspend fun deleteQuote(quoteId: Int)


    @Query("SELECT * FROM quotes WHERE quote_id = :quoteId")
    fun getQuotesById(quoteId: Int): Flow<List<QuoteRoomEntity>>

    @Query("SELECT * FROM quotes WHERE quote_text LIKE '%' || :query || '%'")
    fun getQuotesByText(query: String): Flow<List<QuoteRoomEntity>>

    @Query("SELECT * FROM quotes WHERE artist_id = :artistId")
    fun getQuotesArtistId(artistId: Int): Flow<List<QuoteRoomEntity>>

    @Query("SELECT * FROM quotes WHERE video_id = :videoId")
    fun getQuotesVideoId(videoId: Int): Flow<List<QuoteRoomEntity>>

}