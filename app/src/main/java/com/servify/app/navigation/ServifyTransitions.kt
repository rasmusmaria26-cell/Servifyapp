package com.servify.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.servify.app.core.RenderCapabilities

/**
 * Navigation transition specs.
 *
 * Customer mode: Cinematic, eased slide transitions (600ms, FastOutSlowIn).
 * Vendor mode + Reduced motion: Instant crossfade only (150ms).
 *
 * These are applied at the NavHost composable level.
 * Do NOT override transitions in individual composable() route definitions.
 */
object ServifyTransitions {

    private const val CUSTOMER_DURATION_MS = 600
    private const val VENDOR_DURATION_MS   = 150

    private fun durationMs() =
        if (RenderCapabilities.reduceMotion) VENDOR_DURATION_MS
        else CUSTOMER_DURATION_MS

    // --- Customer (cinematic slide) ---

    val customerEnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec  = tween(durationMillis = CUSTOMER_DURATION_MS)
    ) + fadeIn(animationSpec = tween(durationMillis = CUSTOMER_DURATION_MS))

    val customerExitTransition = slideOutHorizontally(
        targetOffsetX  = { fullWidth -> -fullWidth / 3 },
        animationSpec  = tween(durationMillis = CUSTOMER_DURATION_MS)
    ) + fadeOut(animationSpec = tween(durationMillis = CUSTOMER_DURATION_MS / 2))

    val customerPopEnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec  = tween(durationMillis = CUSTOMER_DURATION_MS)
    ) + fadeIn(animationSpec = tween(durationMillis = CUSTOMER_DURATION_MS))

    val customerPopExitTransition = slideOutHorizontally(
        targetOffsetX  = { fullWidth -> fullWidth },
        animationSpec  = tween(durationMillis = CUSTOMER_DURATION_MS)
    ) + fadeOut(animationSpec = tween(durationMillis = CUSTOMER_DURATION_MS / 2))

    // --- Vendor / Reduced motion (fast crossfade only) ---

    val vendorEnterTransition = fadeIn(animationSpec  = tween(durationMillis = VENDOR_DURATION_MS))
    val vendorExitTransition  = fadeOut(animationSpec = tween(durationMillis = VENDOR_DURATION_MS))
}
