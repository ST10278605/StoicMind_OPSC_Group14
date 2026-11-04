package com.example.stoicmind

import android.app.Application
import com.example.stoicmind.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StoicMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels for real-time alerts
        NotificationHelper.createNotificationChannels(this)
    }
}