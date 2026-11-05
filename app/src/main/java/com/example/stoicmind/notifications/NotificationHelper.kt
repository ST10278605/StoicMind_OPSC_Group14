package com.example.stoicmind.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.stoicmind.R
import com.example.stoicmind.activities.DashboardActivity

object NotificationHelper {

    const val CHANNEL_ID_ALERTS = "stoicmind_realtime_alerts"
    private const val CHANNEL_NAME_ALERTS = "Stoic Mind Real-Time Alerts"
    private const val CHANNEL_DESC_ALERTS = "Notifications for daily stoic wisdom and real-time alerts"

    /**
     * Create notification channels for Android O (API 26) and above.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_ALERTS
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a system notification with title, body, and tap intent.
     */
    fun showNotification(
        context: Context,
        title: String,
        body: String,
        targetIntent: Intent? = null
    ) {
        val intent = targetIntent ?: Intent(context, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (System.currentTimeMillis() % 10000).toInt()
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Notification permission missing or revoked
            e.printStackTrace()
        }
    }
}
