package com.example.surakshasaathi.riskengine

import com.example.surakshasaathi.network.SurakshaApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Hybrid local + cloud threat intelligence & reputation database.
 * Supports on-device zero-latency lookups with automatic cloud delta synchronization
 * against the FastAPI backend when internet connectivity is active.
 */
object LocalReputationDb {

    data class NumberReputation(
        val number: String,
        val category: String,
        val riskScore: Int,
        val reportCount: Int,
    )

    data class DomainReputation(
        val domain: String,
        val category: String,
        val riskScore: Int,
        val reportCount: Int,
    )

    data class CommunityReport(
        val target: String, // phone or domain
        val targetType: String, // "number", "domain", "message"
        val category: String, // "Fake bank", "Fake police", etc.
        val description: String,
        val timestamp: Long = System.currentTimeMillis(),
    )

    // Preloaded known scam numbers
    private val baseKnownNumbers = mapOf(
        "+919876543210" to NumberReputation(
            number = "+919876543210",
            category = "Fake bank call — pretends to be SBI",
            riskScore = 80,
            reportCount = 45,
        ),
        "+919876543211" to NumberReputation(
            number = "+919876543211",
            category = "Suspicious — possible investment fraud",
            riskScore = 55,
            reportCount = 12,
        ),
        "+919876543212" to NumberReputation(
            number = "+919876543212",
            category = "Reported impersonation — pretends to be police",
            riskScore = 90,
            reportCount = 78,
        ),
        "+918001234567" to NumberReputation(
            number = "+918001234567",
            category = "Lottery / prize scam",
            riskScore = 75,
            reportCount = 34,
        ),
    )

    // Preloaded known malicious domains
    private val baseKnownDomains = mapOf(
        "sbi-login-verification.xyz" to DomainReputation(
            domain = "sbi-login-verification.xyz",
            category = "Fake bank phishing portal",
            riskScore = 95,
            reportCount = 112,
        ),
        "bit.ly/sbi-kyc" to DomainReputation(
            domain = "bit.ly/sbi-kyc",
            category = "Bank KYC credential harvester",
            riskScore = 90,
            reportCount = 67,
        ),
        "electricity-bill-pay.site" to DomainReputation(
            domain = "electricity-bill-pay.site",
            category = "Electricity disconnection extortion",
            riskScore = 85,
            reportCount = 43,
        ),
    )

    private val dynamicNumbers = ConcurrentHashMap<String, NumberReputation>()
    private val dynamicDomains = ConcurrentHashMap<String, DomainReputation>()
    private val communityReports = mutableListOf<CommunityReport>()

    private val _syncStatus = MutableStateFlow("Offline Shield Active")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    @Volatile
    var lastSyncTimestamp: Long = 0L
        private set

    private val dbScope = CoroutineScope(Dispatchers.IO)

    /**
     * Synchronous zero-latency check against offline database
     */
    fun checkNumber(number: String): NumberReputation? {
        val clean = number.replace(Regex("[\\s\\-()]"), "")
        val all = baseKnownNumbers + dynamicNumbers
        return all[clean]
            ?: all["+91$clean"]
            ?: all[clean.removePrefix("91").let { "+91$it" }]
    }

    /**
     * Synchronous zero-latency domain check against offline database
     */
    fun checkDomain(domain: String): DomainReputation? {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").substringBefore("/")
        val all = baseKnownDomains + dynamicDomains
        return all[clean]
    }

    /**
     * Adds community report to local database and triggers async cloud sync
     */
    fun addCommunityReport(report: CommunityReport) {
        communityReports.add(0, report)

        when (report.targetType) {
            "number" -> {
                val clean = report.target.replace(Regex("[\\s\\-()]"), "")
                val existing = checkNumber(clean)
                val newCount = (existing?.reportCount ?: 0) + 1
                dynamicNumbers[clean] = NumberReputation(
                    number = clean,
                    category = report.category,
                    riskScore = minOf(95, (existing?.riskScore ?: 60) + 10),
                    reportCount = newCount,
                )
                // Asynchronously push to FastAPI cloud backend
                dbScope.launch {
                    try {
                        SurakshaApiClient.reportNumber(
                            number = clean,
                            category = report.category,
                            description = report.description
                        )
                    } catch (_: Exception) {
                        // Remains safely stored in local database
                    }
                }
            }
            "domain" -> {
                val clean = report.target.trim().lowercase().removePrefix("https://").removePrefix("http://").substringBefore("/")
                val existing = checkDomain(clean)
                val newCount = (existing?.reportCount ?: 0) + 1
                dynamicDomains[clean] = DomainReputation(
                    domain = clean,
                    category = report.category,
                    riskScore = minOf(95, (existing?.riskScore ?: 60) + 10),
                    reportCount = newCount,
                )
                // Asynchronously push to FastAPI cloud backend
                dbScope.launch {
                    try {
                        SurakshaApiClient.reportDomain(
                            domain = clean,
                            category = report.category,
                            description = report.description
                        )
                    } catch (_: Exception) {
                        // Remains safely stored in local database
                    }
                }
            }
        }
    }

    /**
     * Delta synchronizes threat intelligence from FastAPI server into local cache
     */
    suspend fun syncThreatUpdates(): Result<Int> {
        _syncStatus.value = "Connecting to Cloud Threat Engine..."
        val result = SurakshaApiClient.fetchThreatUpdates(sinceTimestamp = lastSyncTimestamp)

        return result.fold(
            onSuccess = { delta ->
                var updatedCount = 0
                delta.numbers.forEach { item ->
                    dynamicNumbers[item.number] = NumberReputation(
                        number = item.number,
                        category = item.category,
                        riskScore = item.riskScore,
                        reportCount = item.reportCount,
                    )
                    updatedCount++
                }
                delta.domains.forEach { item ->
                    dynamicDomains[item.domain] = DomainReputation(
                        domain = item.domain,
                        category = item.category,
                        riskScore = item.riskScore,
                        reportCount = item.reportCount,
                    )
                    updatedCount++
                }
                lastSyncTimestamp = delta.serverTime
                _syncStatus.value = "Synced with Cloud ($updatedCount threats active)"
                Result.success(updatedCount)
            },
            onFailure = { err ->
                _syncStatus.value = "Offline Shield Active (Local DB)"
                Result.failure(err)
            }
        )
    }

    fun getAllKnownScamNumbers(): List<NumberReputation> = (baseKnownNumbers + dynamicNumbers).values.toList()

    fun getAllKnownScamDomains(): List<DomainReputation> = (baseKnownDomains + dynamicDomains).values.toList()

    fun getRecentCommunityReports(): List<CommunityReport> = communityReports.toList()
}
