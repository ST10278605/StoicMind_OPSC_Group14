package com.example.stoicmind.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class StoicMindMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "StoicMindFCM"
    }

    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM device token: $token")
        // Token can be sent to backend server or saved to Firestore user document
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Incoming FCM message from: ${remoteMessage.from}")

        var title = "Stoic Mind Alert"
        var body = "You have a new real-time reflection update."

        // 1. Notification payload handling
        remoteMessage.notification?.let { notification ->
            title = notification.title ?: title
            body = notification.body ?: body
        }

        // 2. Data payload handling (overrides or sets missing fields)
        if (remoteMessage.data.isNotEmpty()) {
            if (remoteMessage.data.containsKey("title")) {
                title = remoteMessage.data["title"] ?: title
            }
            if (remoteMessage.data.containsKey("body")) {
                body = remoteMessage.data["body"] ?: body
            }
        }

        // Show the real-time notification
        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body
        )
    }
}
