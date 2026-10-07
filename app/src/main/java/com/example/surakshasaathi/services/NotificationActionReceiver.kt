package com.example.surakshasaathi.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.surakshasaathi.MainActivity

/**
 * Handles taps on notification action buttons (Ask Family, Dismiss, Open App).
 *
 * Registered in the manifest as an explicit-intent receiver.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ASK_FAMILY = "com.example.surakshasaathi.ACTION_ASK_FAMILY"
        const val ACTION_DISMISS = "com.example.surakshasaathi.ACTION_DISMISS"
        const val ACTION_OPEN_APP = "com.example.surakshasaathi.ACTION_OPEN_APP"
        const val EXTRA_NOTIFICATION_ID = "notification_id"

        // Demo trusted contact — matches TrustedContactsScreen
        private const val FAMILY_PHONE = "+919999999901" // Rahul (Son)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent.action) {
            ACTION_ASK_FAMILY -> {
                Log.d("SurakshaNotif", "Ask Family → dialing $FAMILY_PHONE")
                val dial = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$FAMILY_PHONE")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dial)
                nm.cancel(notifId)
            }

            ACTION_DISMISS -> {
                Log.d("SurakshaNotif", "Dismiss → canceling notification $notifId")
                nm.cancel(notifId)
            }

            ACTION_OPEN_APP -> {
                Log.d("SurakshaNotif", "Open App tapped")
                val launch = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launch)
                nm.cancel(notifId)
            }
        }
    }
}
