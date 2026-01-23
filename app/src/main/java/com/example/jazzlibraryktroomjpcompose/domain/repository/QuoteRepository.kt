package com.example.jazzlibraryktroomjpcompose.domain.repository

import com.example.jazzlibraryktroomjpcompose.domain.models.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {

    // Local operations
    fun getAllQuotes(): Flow<List<Quote>>
    fun getQuoteById(id: Int): Flow<Quote?>
    fun searchQuotes(query: String): Flow<List<Quote>>
    fun getQuotesByArtistId(artistId: Int): Flow<List<Quote>>
    fun getQuotesByVideoId(videoId: Int): Flow<List<Quote>> // Added
    fun getQuotesWithoutArtist(): Flow<List<Quote>> // Added
    fun getQuotesWithoutVideo(): Flow<List<Quote>> // Added

    suspend fun saveQuote(quote: Quote) // Fixed parameter name
    suspend fun deleteQuote(quote: Quote) // Fixed parameter name

    // Remote operations
    suspend fun refreshQuotes(): Result<Unit>
    suspend fun syncQuote(quoteId: Int): Result<Quote>

    // Combined operations
    suspend fun fetchAndCacheQuotes(): Result<List<Quote>>
}