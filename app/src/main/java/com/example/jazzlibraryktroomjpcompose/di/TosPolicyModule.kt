// app/src/main/java/com/example/jazzlibraryktroomjpcompose/di/TosPolicyModule.kt

package com.example.jazzlibraryktroomjpcompose.di

import android.content.Context
import com.example.jazzlibraryktroomjpcompose.data.TosPolicyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TosPolicyModule {

    @Singleton
    @Provides
    fun provideTosPolicyManager(
        @ApplicationContext context: Context
    ): TosPolicyManager {
        return TosPolicyManager(context)
    }
}