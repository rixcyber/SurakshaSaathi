package com.example.surakshasaathi.services

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.example.surakshasaathi.riskengine.RiskEngine
import com.example.surakshasaathi.riskengine.RiskVerdict

/**
 * System call-screening service that is invoked by Android for every
 * incoming call when the user grants Suraksha Saathi the
 * ROLE_CALL_SCREENING role.
 *
 * The service must respond quickly (< ~5 s) or the system will let the
 * call through. All analysis uses the local Risk Engine and the local
 * reputation database — no network call is made.
 *
 * Behaviour:
 *  • STOP  → silence the ring  + post heads-up warning notification
 *  • VERIFY → let the call ring + post a caution notification
 *  • PROCEED → allow silently (no notification)
 *
 * The service never blocks or rejects a call outright — the senior
 * always has the final say.
 */
class SurakshaCallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "SurakshaCallScreen"
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: ""

        if (number.isBlank()) {
            respondToCall(callDetails, allowResponse())
            return
        }

        Log.d(TAG, "Screening call from: ${number.take(6)}***")

        val result = RiskEngine.analyzePhoneNumber(number)

        when (result.verdict) {
            RiskVerdict.STOP -> {
                Log.w(TAG, "🛑 STOP — high-risk caller $number (score ${result.score})")
                NotificationHelper.postCallWarning(this, number, result)
                // Silence the ringer so the senior isn't startled,
                // but do NOT reject the call — they can still pick up.
                respondToCall(
                    callDetails,
                    CallResponse.Builder()
                        .setDisallowCall(false)
                        .setRejectCall(false)
                        .setSilenceCall(true)
                        .setSkipNotification(false)
                        .build(),
                )
            }

            RiskVerdict.VERIFY -> {
                Log.w(TAG, "⚠️ VERIFY — suspicious caller $number (score ${result.score})")
                NotificationHelper.postCallWarning(this, number, result)
                respondToCall(callDetails, allowResponse())
            }

            RiskVerdict.PROCEED -> {
                Log.d(TAG, "✅ PROCEED — safe caller ${number.take(6)}***")
                respondToCall(callDetails, allowResponse())
            }
        }
    }

    /** Let the call through normally. */
    private fun allowResponse(): CallResponse =
        CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(false)
            .setSkipNotification(false)
            .build()
}
