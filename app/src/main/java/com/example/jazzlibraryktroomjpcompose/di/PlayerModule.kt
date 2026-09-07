package com.example.jazzlibraryktroomjpcompose.di

import com.example.jazzlibraryktroomjpcompose.data.player.YouTubePlayerControllerImpl
import com.example.jazzlibraryktroomjpcompose.domain.player.VideoPlayerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

/*  If you rotate your phone, the activity is recreated. But the YouTube player should continue
playing – so we keep it in a component that survives rotation. */

@Module
@InstallIn(ActivityRetainedComponent::class)
object PlayerModule {

    @Provides
    @ActivityRetainedScoped
    fun provideVideoPlayerController(
        @PlayerCoroutineScope coroutineScope: CoroutineScope
    ): VideoPlayerController {
        return YouTubePlayerControllerImpl(coroutineScope)
    }

    @Provides
    @ActivityRetainedScoped
    @PlayerCoroutineScope
    fun providePlayerCoroutineScope(): CoroutineScope {
        // This scope will be tied to the ActivityRetainedComponent lifecycle
        return CoroutineScope(SupervisorJob())
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PlayerCoroutineScope