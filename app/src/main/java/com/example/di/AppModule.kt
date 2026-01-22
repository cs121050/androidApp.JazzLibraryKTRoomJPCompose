package com.example.di


import android.content.Context
import com.example.jazzlibraryktroomjpcompose.data.local.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.remote.RetrofitClient
import com.example.jazzlibraryktroomjpcompose.data.remote.api.JazzApiService
import com.example.jazzlibraryktroomjpcompose.data.repository.ArtistRepositoryImpl
import com.example.jazzlibraryktroomjpcompose.domain.repository.ArtistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJazzDatabase(@ApplicationContext context: Context): JazzDatabase {
        return JazzDatabase.getDatabase(context)
    }

    //TODO//
    @Provides
    @Singleton
    fun provideJazzApiService(): JazzApiService {
//        return RetrofitClient.create()
        return TODO("Provide the return value")
    }

    @Provides
    @Singleton
    fun provideArtistRepository(
        database: JazzDatabase,
        apiService: JazzApiService
    ): ArtistRepository {
        return ArtistRepositoryImpl(database, apiService)
    }
}