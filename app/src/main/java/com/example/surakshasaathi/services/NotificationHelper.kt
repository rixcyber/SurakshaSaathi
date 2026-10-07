package com.example.surakshasaathi.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.surakshasaathi.MainActivity
import com.example.surakshasaathi.R
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.riskengine.RiskVerdict

/**
 * Centralised notification helper for Suraksha Saathi warning alerts.
 *
 * Both Call Shield and Message Shield post heads-up notifications through
 * this helper so that the warning appearance is consistent.
 */
object NotificationHelper {

    const val CHANNEL_CALL_WARNING = "suraksha_call_warning"
    const val CHANNEL_MESSAGE_WARNING = "suraksha_message_warning"

    private const val NOTIFICATION_ID_CALL_BASE = 1000
    private const val NOTIFICATION_ID_MESSAGE_BASE = 2000
    private var messageCounter = 0

    // ── Channel setup (call once from MainActivity) ─────────────────────

    fun createNotificationChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val callChannel = NotificationChannel(
            CHANNEL_CALL_WARNING,
            "Call Scam Warnings",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Warnings about suspicious incoming calls"
            enableVibration(true)
            setShowBadge(true)
        }

        val messageChannel = NotificationChannel(
            CHANNEL_MESSAGE_WARNING,
            "Message Scam Warnings",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Warnings about suspicious messages and notifications"
            enableVibration(true)
            setShowBadge(true)
        }

        nm.createNotificationChannels(listOf(callChannel, messageChannel))
    }

    // ── Call warning ────────────────────────────────────────────────────

    fun postCallWarning(context: Context, phoneNumber: String, result: RiskResult) {
        if (!canPostNotifications(context)) return

        val notificationId = NOTIFICATION_ID_CALL_BASE +
            (phoneNumber.hashCode() and 0x7FFFFFFF) % 500

        val title = when (result.verdict) {
            RiskVerdict.STOP -> "🛑 STOP — Suspicious Caller!"
            RiskVerdict.VERIFY -> "⚠️ VERIFY — Unknown Caller"
            RiskVerdict.PROCEED -> "✅ Call looks safe"
        }

        val reasonText = result.reasons.joinToString("\n• ", prefix = "• ")

        val notification = NotificationCompat.Builder(context, CHANNEL_CALL_WARNING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Number: $phoneNumber")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Number: $phoneNumber\n\n$reasonText")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentIntent(openAppPending(context, notificationId))
            .addAction(0, "📞 Ask Family", askFamilyPending(context, notificationId))
            .addAction(0, "🔍 Check", openAppPending(context, notificationId))
            .addAction(0, "✕ Dismiss", dismissPending(context, notificationId))
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }

    // ── Message warning ─────────────────────────────────────────────────

    fun postMessageWarning(
        context: Context,
        senderTitle: String,
        messageText: String,
        result: RiskResult,
    ) {
        if (!canPostNotifications(context)) return

        messageCounter++
        val notificationId = NOTIFICATION_ID_MESSAGE_BASE + messageCounter

        val title = when (result.verdict) {
            RiskVerdict.STOP -> "⚠️ SURAKSHA SAATHI — Dangerous Message!"
            RiskVerdict.VERIFY -> "⚠️ SURAKSHA SAATHI — Be Careful"
            RiskVerdict.PROCEED -> "Message looks safe"
        }

        val reasonText = result.reasons.joinToString("\n• ", prefix = "• ")

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGE_WARNING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("From: $senderTitle")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("From: $senderTitle\n\n$reasonText")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(openAppPending(context, notificationId))
            .addAction(0, "🔍 Check", openAppPending(context, notificationId))
            .addAction(0, "📞 Ask Family", askFamilyPending(context, notificationId))
            .addAction(0, "✕ Dismiss", dismissPending(context, notificationId))
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }

    // ── PendingIntent factories ─────────────────────────────────────────

    private fun openAppPending(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun askFamilyPending(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_ASK_FAMILY
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context, notificationId + 10_000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun dismissPending(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DISMISS
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context, notificationId + 20_000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    // ── Permission check ────────────────────────────────────────────────

    private fun canPostNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Pre-API 33: notification permission is granted by default
        }
    }
}
