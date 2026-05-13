package com.example.jazzlibraryktroomjpcompose.di

import android.content.Context
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.remote.api.JazzApiService
import com.example.jazzlibraryktroomjpcompose.data.remote.api.RetrofitClient
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

    // Provide the Retrofit API service
    @Provides
    @Singleton
    fun provideJazzApiService(): JazzApiService {
        return RetrofitClient.jazzApiService
    }

    // REMOVE the provideArtistRepository function since we're not using it
    // and it's causing confusion with JazzRepository
}