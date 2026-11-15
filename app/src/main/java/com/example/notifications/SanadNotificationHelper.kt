package com.example.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity
import com.example.R
import java.util.concurrent.TimeUnit

object SanadNotificationHelper {

    const val CHANNEL_MESSAGES = "sanad_messages_channel"
    const val CHANNEL_URGENT = "sanad_urgent_requests_channel"

    const val EXTRA_CONVERSATION_ID = "extra_conversation_id"
    const val EXTRA_OPEN_AS_SPECIALIST = "extra_open_as_specialist"

    private const val PREFS_NAME = "sanad_notification_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_PERMISSION_PROMPTED = "permission_prompted"
    private const val KEY_PREFIX_LAST_NOTIFIED = "last_notified_"
    private const val WORK_NAME_MESSAGE_CHECK = "sanad_message_check_work"

    private const val BRAND_ACCENT_ARGB = 0xFFE07A5F.toInt()

    fun ensureNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "رسائل المحادثات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات فورية عند وصول رسالة جديدة في محادثات الدعم النفسي"
                enableVibration(true)
                setShowBadge(true)
            }

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "طلبات الدعم العاجل",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات فورية لطلبات الدعم العاجل والواردة للأخصائيين"
                enableVibration(true)
                vibrationPattern = longArrayOf(0L, 250L, 120L, 250L)
                setShowBadge(true)
            }

            manager.createNotificationChannel(messagesChannel)
            manager.createNotificationChannel(urgentChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }

    fun wasPermissionPrompted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PERMISSION_PROMPTED, false)
    }

    fun markPermissionPrompted(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PERMISSION_PROMPTED, true)
            .apply()
    }

    fun getLastNotifiedMillis(context: Context, conversationId: String): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong("$KEY_PREFIX_LAST_NOTIFIED$conversationId", 0L)
    }

    fun saveLastNotifiedMillis(context: Context, conversationId: String, timestampMillis: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getLong("$KEY_PREFIX_LAST_NOTIFIED$conversationId", 0L)
        if (timestampMillis > existing) {
            prefs.edit()
                .putLong("$KEY_PREFIX_LAST_NOTIFIED$conversationId", timestampMillis)
                .apply()
        }
    }

    @SuppressLint("MissingPermission")
    fun showIncomingMessageNotification(
        context: Context,
        conversationId: String,
        senderName: String,
        topic: String,
        messageText: String,
        isUrgent: Boolean = false,
        openAsSpecialist: Boolean = false,
        isNewRequest: Boolean = false
    ): Boolean {
        if (!isNotificationsEnabled(context) || !hasNotificationPermission(context)) {
            return false
        }

        ensureNotificationChannels(context)

        val channelId = if (isUrgent || isNewRequest) CHANNEL_URGENT else CHANNEL_MESSAGES
        val notificationId = conversationId.hashCode()

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONVERSATION_ID, conversationId)
            putExtra(EXTRA_OPEN_AS_SPECIALIST, openAsSpecialist)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            isNewRequest && isUrgent -> "طلب دعم عاجل جديد 🚨 • $senderName"
            isNewRequest -> "طلب محادثة جديد • $senderName"
            else -> senderName
        }

        val body = messageText.ifBlank { topic.ifBlank { "رسالة جديدة بانتظارك" } }
        val subText = topic.takeIf { it.isNotBlank() } ?: "كلم سند"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_sanad_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(BRAND_ACCENT_ARGB)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setContentIntent(pendingIntent)

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun cancelConversationNotification(context: Context, conversationId: String) {
        try {
            NotificationManagerCompat.from(context).cancel(conversationId.hashCode())
        } catch (_: Exception) {
            // Ignore
        }
    }

    private fun getOrInitWorkManager(context: Context): WorkManager? {
        return try {
            WorkManager.getInstance(context)
        } catch (_: IllegalStateException) {
            try {
                if (!WorkManager.isInitialized()) {
                    WorkManager.initialize(
                        context.applicationContext,
                        androidx.work.Configuration.Builder().build()
                    )
                }
                WorkManager.getInstance(context)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun scheduleBackgroundMessageSync(context: Context) {
        val workManager = getOrInitWorkManager(context) ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SanadMessageCheckWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_MESSAGE_CHECK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelBackgroundMessageSync(context: Context) {
        getOrInitWorkManager(context)?.cancelUniqueWork(WORK_NAME_MESSAGE_CHECK)
    }
}
