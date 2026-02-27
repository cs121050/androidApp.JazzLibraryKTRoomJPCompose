package com.example.jazzlibraryktroomjpcompose.ui.main.util

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import kotlin.math.abs


class ScrollingSlowFlingBehavior(
    private val speedFactor: Float = 0.3f, // lower = slower
    private val deceleration: Float = 0.01f // standard deceleration value
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        // Reduce the initial velocity
        val slowedVelocity = initialVelocity * speedFactor
        if (abs(slowedVelocity) < 0.1f) return slowedVelocity

        return flingAnimation(slowedVelocity, deceleration)
    }

    private suspend fun ScrollScope.flingAnimation(
        initialVelocity: Float,
        deceleration: Float
    ): Float {
        var velocity = initialVelocity
        var remainingScroll = 0f

        // Animate until velocity is negligible
        while (abs(velocity) > 0.1f && abs(remainingScroll) < 10000f) {
            val scroll = velocity * 0.016f // assume 16ms frame time
            remainingScroll += scroll
            scrollBy(scroll)
            velocity *= (1 - deceleration)
        }
        return remainingScroll
    }
}