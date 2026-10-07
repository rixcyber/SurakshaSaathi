package com.example.surakshasaathi.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight, zero-external-dependency Android network client connecting
 * Suraksha Saathi to the FastAPI Threat Intelligence Backend.
 *
 * Defaults to 10.0.2.2:8000 (standard Android Emulator host alias),
 * but can be overridden at runtime for physical devices or custom servers.
 */
object SurakshaApiClient {

    @Volatile
    var baseUrl: String = "http://10.0.2.2:8000"

    data class NumberCheckResult(
        val number: String,
        val category: String,
        val riskScore: Int,
        val reportCount: Int,
        val confidence: Double,
        val isScam: Boolean,
        val updatedAt: Long,
    )

    data class DomainCheckResult(
        val domain: String,
        val category: String,
        val riskScore: Int,
        val reportCount: Int,
        val confidence: Double,
        val isScam: Boolean,
        val updatedAt: Long,
    )

    data class ThreatDelta(
        val serverTime: Long,
        val totalNumbers: Int,
        val totalDomains: Int,
        val numbers: List<NumberCheckResult>,
        val domains: List<DomainCheckResult>,
    )

    suspend fun isServerReachable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/health")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 1500
                readTimeout = 1500
                requestMethod = "GET"
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }

    suspend fun checkNumber(number: String): Result<NumberCheckResult> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply { put("number", number) }
            val responseStr = postJson("$baseUrl/check-number", payload.toString(), timeoutMs = 2500)
            val json = JSONObject(responseStr)
            Result.success(
                NumberCheckResult(
                    number = json.getString("number"),
                    category = json.getString("category"),
                    riskScore = json.getInt("riskScore"),
                    reportCount = json.getInt("reportCount"),
                    confidence = json.optDouble("confidence", 0.5),
                    isScam = json.getBoolean("isScam"),
                    updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkDomain(domain: String): Result<DomainCheckResult> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply { put("domain", domain) }
            val responseStr = postJson("$baseUrl/check-domain", payload.toString(), timeoutMs = 2500)
            val json = JSONObject(responseStr)
            Result.success(
                DomainCheckResult(
                    domain = json.getString("domain"),
                    category = json.getString("category"),
                    riskScore = json.getInt("riskScore"),
                    reportCount = json.getInt("reportCount"),
                    confidence = json.optDouble("confidence", 0.5),
                    isScam = json.getBoolean("isScam"),
                    updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportNumber(
        number: String,
        category: String,
        description: String,
        reporterId: String? = null,
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("number", number)
                put("category", category)
                put("description", description)
                if (reporterId != null) put("reporterId", reporterId)
            }
            postJson("$baseUrl/report-number", payload.toString(), timeoutMs = 3000)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportDomain(
        domain: String,
        category: String,
        description: String,
        reporterId: String? = null,
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("domain", domain)
                put("category", category)
                put("description", description)
                if (reporterId != null) put("reporterId", reporterId)
            }
            postJson("$baseUrl/report-domain", payload.toString(), timeoutMs = 3000)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchThreatUpdates(sinceTimestamp: Long = 0L): Result<ThreatDelta> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/threat-updates?since_timestamp=$sinceTimestamp")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode !in 200..299) {
                conn.disconnect()
                return@withContext Result.failure(Exception("HTTP error ${conn.responseCode}"))
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val json = JSONObject(response)
            val numbersList = mutableListOf<NumberCheckResult>()
            val domainsList = mutableListOf<DomainCheckResult>()

            val numbersArr = json.optJSONArray("numbers") ?: JSONArray()
            for (i in 0 until numbersArr.length()) {
                val item = numbersArr.getJSONObject(i)
                numbersList.add(
                    NumberCheckResult(
                        number = item.getString("number"),
                        category = item.getString("category"),
                        riskScore = item.getInt("riskScore"),
                        reportCount = item.getInt("reportCount"),
                        confidence = item.optDouble("confidence", 0.5),
                        isScam = item.getBoolean("isScam"),
                        updatedAt = item.optLong("updatedAt", 0L),
                    )
                )
            }

            val domainsArr = json.optJSONArray("domains") ?: JSONArray()
            for (i in 0 until domainsArr.length()) {
                val item = domainsArr.getJSONObject(i)
                domainsList.add(
                    DomainCheckResult(
                        domain = item.getString("domain"),
                        category = item.getString("category"),
                        riskScore = item.getInt("riskScore"),
                        reportCount = item.getInt("reportCount"),
                        confidence = item.optDouble("confidence", 0.5),
                        isScam = item.getBoolean("isScam"),
                        updatedAt = item.optLong("updatedAt", 0L),
                    )
                )
            }

            Result.success(
                ThreatDelta(
                    serverTime = json.optLong("serverTime", System.currentTimeMillis()),
                    totalNumbers = json.optInt("totalNumbers", numbersList.size),
                    totalDomains = json.optInt("totalDomains", domainsList.size),
                    numbers = numbersList,
                    domains = domainsList,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun postJson(urlString: String, jsonBody: String, timeoutMs: Int = 3000): String {
        val url = URL(urlString)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use {
            it.write(jsonBody)
            it.flush()
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
        conn.disconnect()

        if (code !in 200..299) {
            throw Exception("Server returned code $code: $response")
        }
        return response
    }
}
