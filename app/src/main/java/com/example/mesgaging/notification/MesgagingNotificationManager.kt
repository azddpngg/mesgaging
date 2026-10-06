package com.example.mesgaging.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

class MesgagingNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_MESSAGES = "mesgaging_channel_messages"
        const val CHANNEL_CALLS = "mesgaging_channel_calls"
        const val CHANNEL_UPLOADS = "mesgaging_channel_uploads"

        const val NOTIFICATION_ID_CALL = 2001
        const val NOTIFICATION_ID_UPLOAD_BASE = 3000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages & Chats",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming and sent chat messages"
                enableVibration(true)
                setShowBadge(true)
            }

            val callsChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Voice Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for active and incoming voice calls"
                enableVibration(true)
                setShowBadge(true)
            }

            val uploadsChannel = NotificationChannel(
                CHANNEL_UPLOADS,
                "Cloud Storage",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Status for cloud file and image uploads"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(messagesChannel)
            notificationManager.createNotificationChannel(callsChannel)
            notificationManager.createNotificationChannel(uploadsChannel)
        }
    }

    fun showMessageNotification(
        senderName: String,
        messageText: String,
        avatarEmoji: String,
        channelId: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_channel_id", channelId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            channelId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("$avatarEmoji $senderName")
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: SecurityException) {
            // Handled when notification permission is denied
        }
    }

    fun showCloudUploadNotification(fileName: String, cloudUrl: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(cloudUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            cloudUrl.hashCode(),
            browserIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_UPLOADS)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("☁️ Cloud Upload Complete")
            .setContentText("$fileName is live on the cloud!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("File: $fileName\nLink: $cloudUrl"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_UPLOAD_BASE + (fileName.hashCode() % 100), notification)
        } catch (_: SecurityException) {
        }
    }

    fun showCallNotification(callerName: String, isConnected: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_CALL,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val status = if (isConnected) "Call in progress" else "Connecting call..."

        val notification = NotificationCompat.Builder(context, CHANNEL_CALLS)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle("📞 Voice Call with $callerName")
            .setContentText(status)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CALL, notification)
        } catch (_: SecurityException) {
        }
    }

    fun cancelCallNotification() {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_CALL)
        } catch (_: Exception) {
        }
    }
}
