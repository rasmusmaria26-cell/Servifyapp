package com.servify.app

import android.app.Application
import com.servify.app.core.RenderCapabilities
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ServifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // MUST be called here — hardware flags only (blur, shader, RAM, reduced motion).
        // appMode is NOT set here; it is driven reactively by UserSession.switchMode().
        RenderCapabilities.init(applicationContext)
    }
}
