package com.servify.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ServifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
