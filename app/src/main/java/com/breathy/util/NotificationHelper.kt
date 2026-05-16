package com.breathy.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.breathy.MainActivity
import com.breathy.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for creating and managing Breathy's five notification channels
 * and for building and posting notifications with correct deep-link routing.
 *
 * Channels:
 * 1. Motivation  — daily quotes, streak reminders (IMPORTANCE_DEFAULT)
 * 2. Milestones  — achievement unlocks, smoke-free anniversaries (IMPORTANCE_DEFAULT)
 * 3. Social      — friend requests, story likes, comments (IMPORTANCE_HIGH)
 * 4. Chat        — new direct messages (IMPORTANCE_HIGH)
 * 5. Events      — challenge reminders, check-in deadlines, approvals (IMPORTANCE_HIGH)
 */
@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    companion object {
        const val CHANNEL_MOTIVATION = "motivation"
        const val CHANNEL_MILESTONES = "milestones"
        const val CHANNEL_SOCIAL = "social"
        const val CHANNEL_CHAT = "chat"
        const val CHANNEL_EVENTS = "events"

        const val GROUP_SOCIAL = "com.breathy.SOCIAL"
        const val GROUP_CHAT = "com.breathy.CHAT"
        const val GROUP_EVENTS = "com.breathy.EVENTS"

        private const val REQUEST_CODE_MOTIVATION = 1001
        private const val REQUEST_CODE_MILESTONE = 1002
        private const val REQUEST_CODE_SOCIAL = 1003
        private const val REQUEST_CODE_CHAT = 1004
        private const val REQUEST_CODE_EVENT = 1005
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    /**
     * Create all notification channels. Safe to call multiple times —
     * creating an existing channel performs no operation.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_MOTIVATION,
                "Motivation",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily motivational quotes and streak reminders"
                enableVibration(false)
                setShowBadge(true)
            },
            NotificationChannel(
                CHANNEL_MILESTONES,
                "Milestones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Achievement unlocks and smoke-free anniversaries"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                setShowBadge(true)
            },
            NotificationChannel(
                CHANNEL_SOCIAL,
                "Social",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Friend requests, story likes, and comments"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300)
                setShowBadge(true)
            },
            NotificationChannel(
                CHANNEL_CHAT,
                "Chat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New direct messages from friends"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 100)
                setShowBadge(true)
            },
            NotificationChannel(
                CHANNEL_EVENTS,
                "Events",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Challenge start reminders, check-in deadlines, and admin approvals"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 300)
                setShowBadge(true)
            }
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { manager.createNotificationChannel(it) }
    }

    /**
     * Build a PendingIntent that routes to MainActivity with the given route extra
     * so the navigation framework can deep-link to the correct screen.
     */
    private fun buildDeepLinkPendingIntent(route: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", route)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Show a motivation notification (daily quote, streak reminder).
     */
    fun showMotivationNotification(
        id: Int,
        title: String,
        message: String,
        route: String = "home"
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(buildDeepLinkPendingIntent(route, REQUEST_CODE_MOTIVATION))
            .build()

        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted; silently ignore
        }
    }

    /**
     * Show a milestone notification (achievement unlocked, smoke-free anniversary).
     */
    fun showMilestoneNotification(
        id: Int,
        title: String,
        message: String,
        route: String = "achievements"
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_MILESTONES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(buildDeepLinkPendingIntent(route, REQUEST_CODE_MILESTONE))
            .build()

        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted; silently ignore
        }
    }

    /**
     * Show a social notification (friend request, like, reply).
     */
    fun showSocialNotification(
        id: Int,
        title: String,
        message: String,
        route: String,
        groupKey: String = GROUP_SOCIAL
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SOCIAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup(groupKey)
            .setContentIntent(buildDeepLinkPendingIntent(route, REQUEST_CODE_SOCIAL))
            .build()

        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted; silently ignore
        }
    }

    /**
     * Show a chat notification (new message from a friend).
     */
    fun showChatNotification(
        id: Int,
        senderName: String,
        messagePreview: String,
        chatId: String,
        senderId: String
    ) {
        val route = "chat/$chatId"
        val fullMessage = "$senderName: $messagePreview"

        val notification = NotificationCompat.Builder(context, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(senderName)
            .setContentText(messagePreview)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup(GROUP_CHAT)
            .setContentIntent(buildDeepLinkPendingIntent(route, REQUEST_CODE_CHAT))
            .build()

        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted; silently ignore
        }
    }

    /**
     * Show an event notification (challenge reminder, check-in approval, etc.).
     */
    fun showEventNotification(
        id: Int,
        title: String,
        message: String,
        route: String,
        groupKey: String = GROUP_EVENTS
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_EVENTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup(groupKey)
            .setContentIntent(buildDeepLinkPendingIntent(route, REQUEST_CODE_EVENT))
            .build()

        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted; silently ignore
        }
    }

    /**
     * Cancel a specific notification by its ID.
     */
    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    /**
     * Cancel all Breathy notifications.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
