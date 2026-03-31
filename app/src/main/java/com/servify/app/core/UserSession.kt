package com.servify.app.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the authenticated user's session state.
 *
 * [activeMode] drives [RenderCapabilities.appMode]. When a user switches
 * between their customer and vendor roles, update this value. The root
 * composable collects [RenderCapabilities.appMode] and recomposes accordingly
 * (swapping MaterialTheme + clearing the NavHost back stack).
 *
 * No Activity restart is needed for a mode switch.
 */
object UserSession {

    private val _activeMode = MutableStateFlow(AppMode.CUSTOMER)
    val activeMode: StateFlow<AppMode> = _activeMode.asStateFlow()

    /**
     * Switches the user's active role. Propagates to [RenderCapabilities.appMode]
     * which triggers a root composable recomposition and theme swap.
     */
    fun switchMode(mode: AppMode) {
        _activeMode.value = mode
        RenderCapabilities.setAppMode(mode)
    }

    /** Called on sign-out to reset to the default customer mode. */
    fun clear() {
        switchMode(AppMode.CUSTOMER)
    }
}
