package com.example.jazzlibraryktroomjpcompose.domain.repository


import com.example.jazzlibraryktroomjpcompose.domain.models.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {

    // Local operations
    fun getAllQuotes(): Flow<List<Quote>>
    fun getQuoteById(id: Int): Flow<Quote?>
    fun searchQuotes(query: String): Flow<List<Quote>>
    fun getQuotesByArtist(quote: Int): Flow<List<Quote>>

    suspend fun saveQuote(artist: Quote)
    suspend fun deleteQuote(artist: Quote)

    // Remote operations
    suspend fun refreshQuotes(): Result<Unit>
    suspend fun syncQuote(quoteId: Int): Result<Quote>

    // Combined operations
    suspend fun fetchAndCacheQuotes(): Result<List<Quote>>
}