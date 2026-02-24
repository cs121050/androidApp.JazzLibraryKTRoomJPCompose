package com.example.jazzlibraryktroomjpcompose.di

import com.example.jazzlibraryktroomjpcompose.data.repository.JazzRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideJazzRepository(
        database: com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
    ): JazzRepositoryImpl {
        return JazzRepositoryImpl(database)
    }
}