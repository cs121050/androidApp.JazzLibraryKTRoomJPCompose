package com.example.jazzlibraryktroomjpcompose.di

import android.util.Log
import com.example.jazzlibraryktroomjpcompose.data.local.db.JazzDatabase
import com.example.jazzlibraryktroomjpcompose.data.repository.JazzRepositoryImpl
import com.example.jazzlibraryktroomjpcompose.data.repository.impl.*
import com.example.jazzlibraryktroomjpcompose.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository

    @Binds
    @Singleton
    abstract fun bindArtistRepository(impl: ArtistRepositoryImpl): ArtistRepository

    @Binds
    @Singleton
    abstract fun bindInstrumentRepository(impl: InstrumentRepositoryImpl): InstrumentRepository

    @Binds
    @Singleton
    abstract fun bindAlbumRepository(impl: AlbumRepositoryImpl): AlbumRepository

    @Binds
    @Singleton
    abstract fun bindDurationRepository(impl: DurationRepositoryImpl): DurationRepository

    @Binds
    @Singleton
    abstract fun bindTypeRepository(impl: TypeRepositoryImpl): TypeRepository

    @Binds
    @Singleton
    abstract fun bindAssociationRepository(impl: AssociationRepositoryImpl): AssociationRepository

    @Binds
    @Singleton
    abstract fun bindFilterPathRepository(impl: FilterPathRepositoryImpl): FilterPathRepository

    // NEW: Bind FilterRepository (domain interface) to JazzRepositoryImpl
    @Binds
    @Singleton
    abstract fun bindFilterRepository(impl: JazzRepositoryImpl): FilterRepository

    companion object {
        // Provide JazzRepositoryImpl as a concrete class for bootstrap operations
        @Provides
        @Singleton
        fun provideJazzRepository(database: JazzDatabase): JazzRepositoryImpl {
            Log.d("DI", "provideJazzRepository called")
            return JazzRepositoryImpl(database)
        }
    }
}