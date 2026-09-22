package com.example.stoicmind

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StoicMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide configurations
    }
}