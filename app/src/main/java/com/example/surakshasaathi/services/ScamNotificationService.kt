package com.example.surakshasaathi.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.surakshasaathi.riskengine.RiskEngine
import com.example.surakshasaathi.riskengine.RiskVerdict

/**
 * Background service that listens to incoming notifications from messaging
 * apps (WhatsApp, SMS, Telegram, etc.) and runs their content through the
 * local Risk Engine.
 *
 * If the text is suspicious, a Suraksha Saathi heads-up warning notification
 * is posted with Ask Family / Check / Dismiss actions.
 *
 * Privacy: all analysis happens on-device. Notification text is never stored
 * or uploaded. Only the risk verdict is used to decide whether to warn.
 *
 * The user must explicitly grant "Notification access" in Android Settings
 * for this service to receive events.
 */
class ScamNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "ScamNotifService"

        /** Packages we monitor for suspicious messages. */
        private val MONITORED_PACKAGES = setOf(
            // WhatsApp
            "com.whatsapp",
            "com.whatsapp.w4b",
            // Google Messages
            "com.google.android.apps.messaging",
            // Default Android SMS / MMS
            "com.android.mms",
            "com.android.messaging",
            // Samsung Messages
            "com.samsung.android.messaging",
            // Telegram
            "org.telegram.messenger",
            // Signal
            "org.thoughtcrime.securesms",
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val pkg = sbn.packageName ?: return

        // Only messaging apps
        if (pkg !in MONITORED_PACKAGES) return

        // Never analyze our own notifications (avoids infinite loop)
        if (pkg == applicationContext.packageName) return

        // Skip ongoing / group-summary notifications
        if (sbn.isOngoing) return

        val extras = sbn.notification?.extras ?: return

        // Extract the richest text available
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        val messageContent = bigText ?: text
        if (messageContent.isBlank()) return

        val fullText = if (title.isNotBlank()) "$title: $messageContent" else messageContent

        Log.d(TAG, "Analyzing notification from $pkg: ${fullText.take(60)}…")

        // ── On-device risk analysis ──
        val result = RiskEngine.analyzeMessage(fullText)

        if (result.verdict != RiskVerdict.PROCEED) {
            Log.w(
                TAG,
                "⚠️ Suspicious message detected! " +
                    "Verdict: ${result.verdict}, Score: ${result.score}",
            )
            NotificationHelper.postMessageWarning(
                context = this,
                senderTitle = title.ifBlank { pkg },
                messageText = messageContent,
                result = result,
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "✅ Suraksha Saathi Message Shield connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i(TAG, "❌ Suraksha Saathi Message Shield disconnected")
    }
}
