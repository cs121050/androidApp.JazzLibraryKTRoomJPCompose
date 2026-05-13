package com.example.jazzlibraryktroomjpcompose.di

import android.content.Context
import com.example.jazzlibraryktroomjpcompose.data.repository.FirebaseRemoteConfigRepository
import com.example.jazzlibraryktroomjpcompose.domain.repository.RemoteConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig
    }

    @Provides
    @Singleton
    fun provideRemoteConfigRepository(
        firebaseRemoteConfig: FirebaseRemoteConfig,
        @ApplicationContext context: Context
    ): RemoteConfigRepository {
        return FirebaseRemoteConfigRepository(firebaseRemoteConfig, context)
    }
}