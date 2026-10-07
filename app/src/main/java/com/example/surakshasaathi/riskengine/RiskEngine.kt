package com.example.surakshasaathi.riskengine

/**
 * Risk verdict shown to the user. Internal scores are never displayed to elders.
 */
enum class RiskVerdict(val label: String) {
    STOP("STOP"),
    VERIFY("VERIFY"),
    PROCEED("PROCEED CAREFULLY");

    companion object {
        fun fromScore(score: Int): RiskVerdict = when {
            score >= 70 -> STOP
            score >= 40 -> VERIFY
            else -> PROCEED
        }
    }
}

/**
 * A single piece of evidence contributing to the risk score.
 */
data class RiskEvidence(
    val indicator: String,  // e.g. "OTP request detected"
    val score: Int,         // points added (0-30 typically)
    val category: String,   // e.g. "urgency", "financial", "link"
)

/**
 * The complete result of a risk analysis.
 */
data class RiskResult(
    val score: Int,
    val verdict: RiskVerdict,
    val evidenceList: List<RiskEvidence>,
) {
    val reasons: List<String> get() = evidenceList.map { it.indicator }
}

/**
 * Central Risk Engine. All shields (Call, Message, Link) use this to produce a
 * consistent STOP / VERIFY / PROCEED CAREFULLY decision.
 *
 * Design principle: combine multiple independent indicators. A single weak
 * indicator should have limited effect; multiple indicators accumulate.
 */
object RiskEngine {

    // ── Suspicious keyword dictionaries ──

    private val urgencyKeywords = listOf(
        "urgent", "immediately", "right now", "hurry", "act now",
        "within 24 hours", "last chance", "expire", "suspend",
        "block", "blocked", "deactivate", "terminate",
        "disconnected", "disconnection", "power cut",
        "turant", "abhi", "jaldi", // Hindi urgency
    )

    private val financialKeywords = listOf(
        "otp", "pin", "cvv", "upi", "neft", "imps", "rtgs",
        "bank details", "account number", "ifsc",
        "kyc", "pan card", "aadhaar", "aadhar",
        "transfer money", "send money", "pay now", "payment",
        "refund", "cashback", "prize", "lottery", "reward",
        "credit card", "debit card",
        "bill", "unpaid bill", "electricity bill", "bijli",
    )

    private val impersonationKeywords = listOf(
        "police", "cbi", "income tax", "customs", "rbi",
        "reserve bank", "sbi", "hdfc", "icici", "axis bank",
        "paytm", "phonepe", "google pay", "gpay",
        "amazon", "flipkart", "whatsapp", "government",
        "arrest", "warrant", "legal notice", "court order",
        "officer", "inspector", "electricity department",
    )

    private val linkActionKeywords = listOf(
        "click here", "click below", "tap here", "open link",
        "verify account", "verify now", "update now",
        "download", "install", "login here",
    )

    // ── Suspicious domain patterns ──

    private val trustedDomains = listOf(
        "sbi.co.in", "onlinesbi.sbi", "hdfcbank.com", "icicibank.com",
        "axisbank.com", "rbi.org.in", "incometax.gov.in",
        "google.com", "facebook.com", "whatsapp.com",
        "amazon.in", "flipkart.com", "paytm.com",
        "phonepe.com", "npci.org.in",
    )

    private val suspiciousTlds = listOf(
        ".xyz", ".top", ".club", ".work", ".click",
        ".link", ".info", ".bid", ".stream", ".gq",
        ".cf", ".tk", ".ml", ".ga",
    )

    // ── Public analysis functions ──

    /**
     * Analyze a text message (SMS / WhatsApp notification / pasted text).
     */
    fun analyzeMessage(text: String): RiskResult {
        val evidence = mutableListOf<RiskEvidence>()
        val lowerText = text.lowercase()

        // Check urgency
        val urgencyHits = urgencyKeywords.filter { lowerText.contains(it) }
        if (urgencyHits.isNotEmpty()) {
            evidence.add(
                RiskEvidence(
                    indicator = "Uses urgent language: ${urgencyHits.take(2).joinToString(", ")}",
                    score = 15,
                    category = "urgency",
                )
            )
        }

        // Check financial / credential requests
        val financialHits = financialKeywords.filter { lowerText.contains(it) }
        if (financialHits.isNotEmpty()) {
            val score = if (financialHits.any { it in listOf("otp", "pin", "cvv") }) 30 else 20
            evidence.add(
                RiskEvidence(
                    indicator = "Asks for sensitive details: ${financialHits.take(3).joinToString(", ")}",
                    score = score,
                    category = "financial",
                )
            )
        }

        // Check impersonation
        val impersonationHits = impersonationKeywords.filter { lowerText.contains(it) }
        if (impersonationHits.isNotEmpty()) {
            evidence.add(
                RiskEvidence(
                    indicator = "May be pretending to be: ${impersonationHits.take(2).joinToString(", ")}",
                    score = 20,
                    category = "impersonation",
                )
            )
        }

        // Check call-to-action links
        val linkActionHits = linkActionKeywords.filter { lowerText.contains(it) }
        if (linkActionHits.isNotEmpty()) {
            evidence.add(
                RiskEvidence(
                    indicator = "Pressures you to click a link",
                    score = 15,
                    category = "link_action",
                )
            )
        }

        // Check if message contains a URL
        val urls = extractUrls(text)
        if (urls.isNotEmpty()) {
            for (url in urls) {
                val urlEvidence = analyzeUrlInternal(url)
                evidence.addAll(urlEvidence)
            }
        }

        val totalScore = evidence.sumOf { it.score }.coerceIn(0, 100)
        return RiskResult(
            score = totalScore,
            verdict = RiskVerdict.fromScore(totalScore),
            evidenceList = evidence,
        )
    }

    /**
     * Analyze a URL / link.
     */
    fun analyzeLink(url: String): RiskResult {
        val evidence = analyzeUrlInternal(url).toMutableList()

        val totalScore = evidence.sumOf { it.score }.coerceIn(0, 100)
        return RiskResult(
            score = totalScore,
            verdict = RiskVerdict.fromScore(totalScore),
            evidenceList = evidence,
        )
    }

    /**
     * Analyze a phone number against the local reputation database.
     */
    fun analyzePhoneNumber(number: String): RiskResult {
        val evidence = mutableListOf<RiskEvidence>()
        val clean = number.replace(Regex("[\\s\\-()]"), "")

        // Check local reputation database
        val reputation = LocalReputationDb.checkNumber(clean)
        if (reputation != null) {
            evidence.add(
                RiskEvidence(
                    indicator = "This number has been reported: ${reputation.category}",
                    score = reputation.riskScore,
                    category = "reputation",
                )
            )
        }

        // Unknown number heuristics
        if (!clean.startsWith("+91") && !clean.startsWith("91") && clean.length > 5) {
            evidence.add(
                RiskEvidence(
                    indicator = "International or unusual number format",
                    score = 15,
                    category = "format",
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

    // ── Internal helpers ──

    private fun analyzeUrlInternal(url: String): List<RiskEvidence> {
        val evidence = mutableListOf<RiskEvidence>()
        val lowerUrl = url.lowercase()

        // Check local domain reputation database
        val domainReputation = LocalReputationDb.checkDomain(url)
        if (domainReputation != null) {
            evidence.add(
                RiskEvidence(
                    indicator = "Known malicious domain: ${domainReputation.category} (${domainReputation.reportCount} reports)",
                    score = domainReputation.riskScore,
                    category = "reputation",
                )
            )
        }

        // Check for IP address URLs
        if (Regex("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""").containsMatchIn(lowerUrl)) {
            evidence.add(
                RiskEvidence(
                    indicator = "Link uses an IP address instead of a website name",
                    score = 30,
                    category = "link",
                )
            )
        }

        // Check for suspicious TLDs
        val hasSuspiciousTld = suspiciousTlds.any { lowerUrl.contains(it) }
        if (hasSuspiciousTld) {
            evidence.add(
                RiskEvidence(
                    indicator = "Link uses a suspicious website extension",
                    score = 25,
                    category = "link",
                )
            )
        }

        // Check for URL shorteners
        val shorteners = listOf("bit.ly", "tinyurl", "t.co", "goo.gl", "is.gd", "short.link", "cutt.ly")
        if (shorteners.any { lowerUrl.contains(it) }) {
            evidence.add(
                RiskEvidence(
                    indicator = "Link is shortened — the real destination is hidden",
                    score = 20,
                    category = "link",
                )
            )
        }

        // Check for look-alike domains (bank names in non-trusted domains)
        val bankNames = listOf("sbi", "hdfc", "icici", "axis", "rbi", "paytm", "phonepe")
        val isTrusted = trustedDomains.any { lowerUrl.contains(it) }
        if (!isTrusted) {
            val hasBankName = bankNames.any { lowerUrl.contains(it) }
            if (hasBankName) {
                evidence.add(
                    RiskEvidence(
                        indicator = "Looks like a bank website but is not the official domain",
                        score = 35,
                        category = "link",
                    )
                )
            }
        }

        // Check HTTP (non-secure)
        if (lowerUrl.startsWith("http://") && !lowerUrl.contains("localhost")) {
            evidence.add(
                RiskEvidence(
                    indicator = "Link is not secure (no HTTPS)",
                    score = 10,
                    category = "link",
                )
            )
        }

        // Check for suspicious characters (Unicode homoglyphs)
        if (url.any { it.code > 127 }) {
            evidence.add(
                RiskEvidence(
                    indicator = "Link contains unusual characters that may be deceptive",
                    score = 25,
                    category = "link",
                )
            )
        }

        // If URL has no specific issues, note it as unknown
        if (evidence.isEmpty() && !isTrusted) {
            evidence.add(
                RiskEvidence(
                    indicator = "This is an unfamiliar website — proceed with caution",
                    score = 15,
                    category = "link",
                )
            )
        }

        return evidence
    }

    private fun extractUrls(text: String): List<String> {
        val urlRegex = Regex("""(https?://[^\s]+|www\.[^\s]+|[a-zA-Z0-9][-a-zA-Z0-9]*\.[a-z]{2,}[^\s]*)""")
        return urlRegex.findAll(text).map { it.value }.toList()
    }
}
