package com.servify.app.core

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central singleton for runtime rendering capabilities.
 *
 * RULES:
 * - [init] MUST be called in Application.onCreate() — hardware flags only.
 * - [appMode] is driven by UserSession, NOT by init(). Never compare appMode inline
 *   as a raw StateFlow; always collect it first:
 *
 *   val appMode by RenderCapabilities.appMode.collectAsStateWithLifecycle()
 *   val useBlur = RenderCapabilities.supportsBlur && appMode == AppMode.CUSTOMER
 *
 * - No inline Build.VERSION.SDK_INT checks elsewhere in the codebase.
 *   All capability flags live here.
 */
object RenderCapabilities {

    // --- Hardware Capability Flags ---

    /** True if Modifier.blur() / RenderEffect is supported (API 31+). */
    var supportsBlur: Boolean = false
        private set

    /**
     * True if AGSL RuntimeEffect Shaders are safe to run.
     * Requires API 33+ AND the device must not be low-RAM.
     */
    var supportsShader: Boolean = false
        private set

    /** True if ActivityManager reports this as a low-RAM device. */
    var isLowRam: Boolean = false
        private set

    /**
     * True if the user has disabled system animations via accessibility settings.
     * When true, Customer mode must behave like Vendor mode for all motion.
     */
    var reduceMotion: Boolean = false
        private set

    // --- App Mode (Session-driven, not init-driven) ---

    private val _appMode = MutableStateFlow(AppMode.CUSTOMER)

    /**
     * Reactive app mode driven by UserSession.
     * Collect in root composable to swap MaterialTheme and NavHost.
     * DO NOT compare this flow inline without collecting it first.
     */
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    /** Called by UserSession when the authenticated user switches roles. */
    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    // --- Initialisation ---

    /**
     * MUST be called once in [Application.onCreate].
     * Resolves hardware capability flags. Does NOT set appMode.
     *
     * @param context Application context.
     */
    fun init(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        isLowRam = activityManager.isLowRamDevice

        supportsBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // API 31

        supportsShader = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isLowRam // API 33

        val animatorScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        reduceMotion = animatorScale == 0f
    }
}
