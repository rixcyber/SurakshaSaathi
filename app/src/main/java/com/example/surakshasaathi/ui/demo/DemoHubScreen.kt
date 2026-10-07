package com.example.surakshasaathi.ui.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surakshasaathi.riskengine.RiskEngine
import com.example.surakshasaathi.services.NotificationHelper
import com.example.surakshasaathi.theme.*

data class DemoScenario(
    val id: Int,
    val title: String,
    val subtitle: String,
    val flowSteps: List<String>,
    val triggerActionLabel: String,
    val emoji: String,
    val color: Color,
)

private val demoScenarios = listOf(
    DemoScenario(
        id = 1,
        title = "Demo 1: Fake Bank SMS",
        subtitle = "Urgency + OTP + look-alike shortlink detection",
        flowSteps = listOf(
            "Fake bank notification arrives: \"SBI account blocked, complete KYC: bit.ly/sbi-kyc\"",
            "Message Shield intercepts notification on-device",
            "Risk Engine accumulates urgency + OTP + shortlink indicators",
            "System fires Heads-up Warning Notification with STOP verdict",
            "Senior taps 'Ask Family' or listens to Voice Guidance",
        ),
        triggerActionLabel = "Simulate Incoming Bank SMS Warning",
        emoji = "💬",
        color = MessageShieldColor,
    ),
    DemoScenario(
        id = 2,
        title = "Demo 2: Phishing URL",
        subtitle = "Look-alike bank domain detection without visiting the page",
        flowSteps = listOf(
            "User receives suspicious link: 'sbi-login-verification.xyz'",
            "Link Shield checks domain reputation and suspicious TLD heuristics",
            "Verdict: STOP — 'Looks like a bank website but is not the official domain'",
            "Senior listens to spoken warning and avoids opening the phishing link",
        ),
        triggerActionLabel = "Test Phishing Link in Link Shield",
        emoji = "🔗",
        color = LinkShieldColor,
    ),
    DemoScenario(
        id = 3,
        title = "Demo 3: Suspicious Caller",
        subtitle = "Call screening with on-device reputation database",
        flowSteps = listOf(
            "Incoming call from +919876543210 (reported SBI impersonation)",
            "Call Screening Service checks LocalReputationDb",
            "Verdict: STOP — call is silenced and warning posted",
            "Heads-up notification lets senior immediately call trusted family contact",
        ),
        triggerActionLabel = "Simulate Suspicious Call Warning",
        emoji = "📞",
        color = CallShieldColor,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoHubScreen(
    onBack: () -> Unit,
    onNavigateToLinkShield: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var lastSimulatedScenario by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "TATA Competition Demo Hub",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurakshaPrimaryLight.copy(alpha = 0.12f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏆 Official Competition Demonstration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SurakshaPrimaryDark,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Use this control hub to demonstrate the 3 core safety scenarios rehearsed for judges and evaluation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            lastSimulatedScenario?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SafeGreenLight,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "✅ $msg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SafeGreen,
                        modifier = Modifier.padding(14.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            demoScenarios.forEach { scenario ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(scenario.emoji, fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scenario.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = scenario.color,
                                )
                                Text(
                                    text = scenario.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Flow steps
                        scenario.flowSteps.forEachIndexed { idx, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                            ) {
                                Text(
                                    text = "${idx + 1}. ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scenario.color,
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                when (scenario.id) {
                                    1 -> {
                                        val sampleMsg = "SBI: Your account will be blocked today. Complete KYC immediately: bit.ly/sbi-kyc. Share OTP to verify."
                                        val result = RiskEngine.analyzeMessage(sampleMsg)
                                        NotificationHelper.postMessageWarning(context, "SBI-Alert", sampleMsg, result)
                                        lastSimulatedScenario = "Fired Heads-Up Notification for Demo 1 (Fake Bank SMS)"
                                    }
                                    2 -> {
                                        onNavigateToLinkShield()
                                    }
                                    3 -> {
                                        val number = "+919876543210"
                                        val result = RiskEngine.analyzePhoneNumber(number)
                                        NotificationHelper.postCallWarning(context, number, result)
                                        lastSimulatedScenario = "Fired Heads-Up Notification for Demo 3 (Suspicious Call from $number)"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = scenario.color,
                            ),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(scenario.triggerActionLabel)
                        }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
