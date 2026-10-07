package com.example.surakshasaathi.riskengine

import org.junit.Assert.*
import org.junit.Test

/**
 * Section 17 Testing Plan implementation.
 * Validates actual behavioral requirements:
 * - False positive prevention
 * - Indicator accumulation
 * - Link heuristics & reputation
 * - Call reputation checks
 * - UPI / QR safety rules
 * - Multilingual keyword detection
 */
class RiskEngineTest {

    // ── 1. False Positive Prevention Tests ─────────────────────────────

    @Test
    fun `normal message containing bank is not automatically a scam`() {
        val message = "Your monthly bank statement is now available to download from your net banking account."
        val result = RiskEngine.analyzeMessage(message)
        assertNotEquals("Should not be STOP", RiskVerdict.STOP, result.verdict)
    }

    @Test
    fun `trusted bank domain with login path is not flagged as malicious`() {
        val trustedUrl = "https://www.onlinesbi.sbi/login.html"
        val result = RiskEngine.analyzeLink(trustedUrl)
        assertEquals("Trusted domain should be PROCEED", RiskVerdict.PROCEED, result.verdict)
    }

    @Test
    fun `single weak indicator has limited effect`() {
        val text = "Please reply when you are free."
        val result = RiskEngine.analyzeMessage(text)
        assertEquals("Should be PROCEED", RiskVerdict.PROCEED, result.verdict)
    }

    // ── 2. Evidence Accumulation Tests ──────────────────────────────────

    @Test
    fun `urgency plus OTP plus unknown link moves to STOP`() {
        val scamMsg = "URGENT: Your SBI card is blocked. Complete KYC immediately at bit.ly/fake-sbi. Share your OTP to verify now."
        val result = RiskEngine.analyzeMessage(scamMsg)
        assertEquals("Multiple critical indicators must produce STOP", RiskVerdict.STOP, result.verdict)
        assertTrue("Must include OTP evidence", result.reasons.any { it.contains("OTP", ignoreCase = true) })
        assertTrue("Must include urgency evidence", result.reasons.any { it.contains("urgent", ignoreCase = true) || it.contains("blocked", ignoreCase = true) })
    }

    @Test
    fun `electricity disconnection threat is detected`() {
        val electricityMsg = "Dear consumer your electricity power will be disconnected tonight at 9:30 PM due to unpaid bill. Call officer immediately."
        val result = RiskEngine.analyzeMessage(electricityMsg)
        assertTrue("Score should be elevated", result.score >= 40)
        assertTrue(result.verdict == RiskVerdict.VERIFY || result.verdict == RiskVerdict.STOP)
    }

    // ── 3. Link Shield Tests ────────────────────────────────────────────

    @Test
    fun `ip address link is detected as suspicious`() {
        val ipUrl = "http://192.168.1.100/secure-login"
        val result = RiskEngine.analyzeLink(ipUrl)
        assertTrue(result.score >= 30)
        assertTrue("Must detect IP address", result.reasons.any { it.contains("IP address", ignoreCase = true) })
    }

    @Test
    fun `shortened url is flagged with warning`() {
        val shortUrl = "https://bit.ly/claim-cash-prize"
        val result = RiskEngine.analyzeLink(shortUrl)
        assertTrue(result.reasons.any { it.contains("shortened", ignoreCase = true) })
    }

    @Test
    fun `bank look-alike domain is flagged as impersonation`() {
        val lookAlikeUrl = "http://sbi-login-verification.xyz"
        val result = RiskEngine.analyzeLink(lookAlikeUrl)
        assertEquals("Bank impersonation domain should be STOP", RiskVerdict.STOP, result.verdict)
    }

    // ── 4. Call Shield & Reputation Tests ───────────────────────────────

    @Test
    fun `known scam caller number in reputation database produces STOP`() {
        val reportedNumber = "+919876543210"
        val result = RiskEngine.analyzePhoneNumber(reportedNumber)
        assertEquals(RiskVerdict.STOP, result.verdict)
        assertTrue(result.reasons.any { it.contains("reported", ignoreCase = true) })
    }

    @Test
    fun `new community report updates reputation database dynamically`() {
        val newThreatNumber = "+919123456789"
        assertNull(LocalReputationDb.checkNumber(newThreatNumber))

        LocalReputationDb.addCommunityReport(
            LocalReputationDb.CommunityReport(
                target = newThreatNumber,
                targetType = "number",
                category = "Fake Police",
                description = "Called claiming son is arrested",
            )
        )

        val rep = LocalReputationDb.checkNumber(newThreatNumber)
        assertNotNull("Should now be in reputation db", rep)
        assertEquals("Fake Police", rep?.category)
    }

    // ── 5. QR & Payment Safety Tests ────────────────────────────────────

    @Test
    fun `entering pin to receive money is detected as severe scam`() {
        val payload = "upi://pay?pa=seller@okaxis&pn=Shop&am=500"
        val parsed = QrRiskEngine.parseQrPayload(payload)
        val result = QrRiskEngine.analyzePayment(parsed, userToldToReceive = true)
        assertEquals("Told to enter PIN to receive money is instant STOP", RiskVerdict.STOP, result.verdict)
        assertTrue(result.reasons.any { it.contains("receive money", ignoreCase = true) })
    }

    @Test
    fun `normal merchant qr without receive scam is proceed`() {
        val payload = "upi://pay?pa=store@okicici&pn=GroceryStore&am=250"
        val parsed = QrRiskEngine.parseQrPayload(payload)
        val result = QrRiskEngine.analyzePayment(parsed, userToldToReceive = false)
        assertEquals("Legitimate merchant payment is PROCEED", RiskVerdict.PROCEED, result.verdict)
    }

    // ── 6. Multilingual Keyword Tests ───────────────────────────────────

    @Test
    fun `hindi urgency keywords trigger risk accumulation`() {
        val hindiMsg = "Turant apna account update karein nahi toh abhi band ho jayega. OTP share karein."
        val result = RiskEngine.analyzeMessage(hindiMsg)
        assertTrue("Score must reflect Hindi urgency + OTP", result.score >= 40)
    }
}
