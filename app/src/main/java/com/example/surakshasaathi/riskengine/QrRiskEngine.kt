package com.example.surakshasaathi.riskengine

import android.net.Uri

data class ParsedUpiDetails(
    val rawPayload: String,
    val payeeVpa: String,
    val payeeName: String,
    val amount: String?,
    val note: String?,
    val isUpiPayment: Boolean,
)

object QrRiskEngine {

    private val suspiciousNameKeywords = listOf(
        "refund", "cashback", "customer care", "helpline", "electricity",
        "bill", "sbi", "hdfc", "icici", "rbi", "bank", "lottery",
        "reward", "prize", "support", "kyc", "police", "customs",
        "income tax", "pension",
    )

    private val suspiciousNoteKeywords = listOf(
        "processing fee", "registration fee", "lottery charge", "tax clearance",
        "customs charge", "refund fee", "unfreeze", "kyc charge",
    )

    /**
     * Parse raw string from QR scan or text input into UPI payment details.
     */
    fun parseQrPayload(payload: String): ParsedUpiDetails {
        val trimmed = payload.trim()
        if (trimmed.startsWith("upi://pay", ignoreCase = true)) {
            return try {
                val queryString = trimmed.substringAfter("?", "")
                val params = queryString.split("&").mapNotNull {
                    val parts = it.split("=", limit = 2)
                    if (parts.size == 2) parts[0].lowercase() to parts[1] else null
                }.toMap()

                val pa = params["pa"] ?: ""
                val pn = params["pn"] ?: ""
                val am = params["am"]
                val tn = params["tn"]

                ParsedUpiDetails(
                    rawPayload = trimmed,
                    payeeVpa = pa,
                    payeeName = pn.replace("+", " "),
                    amount = am,
                    note = tn?.replace("+", " "),
                    isUpiPayment = true,
                )
            } catch (e: Exception) {
                ParsedUpiDetails(trimmed, "", "", null, null, false)
            }
        }

        // Check if raw text looks like a VPA (e.g. name@bank)
        val vpaRegex = Regex("[a-zA-Z0-9.\\-_]+@[a-zA-Z]+")
        val match = vpaRegex.find(trimmed)
        if (match != null) {
            return ParsedUpiDetails(
                rawPayload = trimmed,
                payeeVpa = match.value,
                payeeName = "",
                amount = null,
                note = null,
                isUpiPayment = true,
            )
        }

        return ParsedUpiDetails(
            rawPayload = trimmed,
            payeeVpa = "",
            payeeName = "",
            amount = null,
            note = null,
            isUpiPayment = false,
        )
    }

    /**
     * Analyze a QR code or UPI payment string for scams.
     * @param details Parsed UPI details
     * @param userToldToReceive Whether someone claimed this QR will RECEIVE money
     */
    fun analyzePayment(
        details: ParsedUpiDetails,
        userToldToReceive: Boolean,
    ): RiskResult {
        val evidence = mutableListOf<RiskEvidence>()

        // 1. FUNDAMENTAL FRAUD: "Scan to Receive Money"
        if (userToldToReceive) {
            evidence.add(
                RiskEvidence(
                    indicator = "SCAM ALERT: You NEVER scan a QR code or enter your UPI PIN to receive money. Scanning this will DEDUCT money from your account.",
                    score = 90,
                    category = "receive_trap",
                )
            )
        }

        // 2. Validate UPI format
        if (!details.isUpiPayment) {
            evidence.add(
                RiskEvidence(
                    indicator = "This QR code does not contain a standard UPI payment address.",
                    score = 25,
                    category = "format",
                )
            )
            val score = evidence.sumOf { it.score }.coerceIn(0, 100)
            return RiskResult(score, RiskVerdict.fromScore(score), evidence)
        }

        // 3. Name vs VPA impersonation check
        val lowerName = details.payeeName.lowercase()
        val lowerVpa = details.payeeVpa.lowercase()

        val matchingKeyword = suspiciousNameKeywords.firstOrNull { lowerName.contains(it) }
        if (matchingKeyword != null) {
            // Check if VPA looks like a personal VPA rather than official
            val isOfficialMerchantVpa = lowerVpa.contains("merchant") || lowerVpa.contains("billdesk")
            if (!isOfficialMerchantVpa) {
                evidence.add(
                    RiskEvidence(
                        indicator = "Payee name claims to be \"${details.payeeName}\", but payment goes to a personal address: ${details.payeeVpa}",
                        score = 35,
                        category = "impersonation",
                    )
                )
            }
        }

        // 4. Suspicious transaction note
        details.note?.let { note ->
            val lowerNote = note.lowercase()
            val noteHit = suspiciousNoteKeywords.firstOrNull { lowerNote.contains(it) }
            if (noteHit != null) {
                evidence.add(
                    RiskEvidence(
                        indicator = "Suspicious transaction purpose: \"$note\" (Legitimate services never ask for advance fees)",
                        score = 30,
                        category = "note",
                    )
                )
            }
        }

        // 5. High pre-filled amount
        details.amount?.toDoubleOrNull()?.let { amt ->
            if (amt >= 10000) {
                evidence.add(
                    RiskEvidence(
                        indicator = "High payment amount requested: ₹${amt.toInt()}",
                        score = 20,
                        category = "amount",
                    )
                )
            }
        }

        // If no evidence found, add a clean verification indicator
        if (evidence.isEmpty()) {
            evidence.add(
                RiskEvidence(
                    indicator = "Valid UPI address format (${details.payeeVpa}). No known scam signals detected.",
                    score = 0,
                    category = "clean",
                )
            )
        }

        val totalScore = evidence.sumOf { it.score }.coerceIn(0, 100)
        return RiskResult(
            score = totalScore,
            verdict = RiskVerdict.fromScore(totalScore),
            evidenceList = evidence,
        )
    }
}
