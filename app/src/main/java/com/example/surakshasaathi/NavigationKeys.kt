package com.example.surakshasaathi

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object CallShieldScreen : NavKey

@Serializable data object MessageShieldScreen : NavKey

@Serializable data object LinkShieldScreen : NavKey

@Serializable data object AppShieldScreen : NavKey

@Serializable data object QrShieldScreen : NavKey

@Serializable data object ScreenShieldScreen : NavKey

@Serializable data object CommunityReportScreen : NavKey

@Serializable data object DemoHubScreen : NavKey

@Serializable data object TrustedContactsScreen : NavKey

@Serializable data object EmergencyScreen : NavKey

// Result screens carry the analysis outcome
@Serializable
data class RiskResultScreen(
    val verdict: String,     // "STOP", "VERIFY", "PROCEED"
    val score: Int,          // 0-100 internal score
    val title: String,       // What triggered the analysis
    val reasons: String,     // Pipe-separated reasons
    val shieldType: String,  // "call", "message", "link"
) : NavKey
