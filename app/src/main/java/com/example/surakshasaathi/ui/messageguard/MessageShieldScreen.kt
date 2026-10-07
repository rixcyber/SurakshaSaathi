package com.example.surakshasaathi.ui.messageguard

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.surakshasaathi.riskengine.RiskEngine
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.riskengine.RiskVerdict
import com.example.surakshasaathi.theme.*
import com.example.surakshasaathi.voice.VoiceGuidanceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageShieldScreen(
    onBack: () -> Unit,
    onNavigateToResult: (RiskResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var messageInput by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<RiskResult?>(null) }

    // ── Notification-listener state ──
    var isListenerEnabled by remember { mutableStateOf(false) }

    // Recheck every time the screen is resumed (e.g. after returning
    // from Settings → Notification access)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isListenerEnabled = isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    "Message Protection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(MessageShieldColor, MessageShieldColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("💬", fontSize = 36.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Check a Message",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Paste the suspicious message below.\nWe will check if it might be a scam.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // ── Notification Listener Setup Card ──
            NotificationListenerSetupCard(
                isEnabled = isListenerEnabled,
                onEnable = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                },
            )

            Spacer(Modifier.height(24.dp))

            // Message input
            OutlinedTextField(
                value = messageInput,
                onValueChange = {
                    messageInput = it
                    analysisResult = null
                },
                label = { Text("Paste message here", style = MaterialTheme.typography.bodyLarge) },
                placeholder = {
                    Text(
                        "Copy the SMS or WhatsApp message and paste it here",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(14.dp),
                singleLine = false,
                maxLines = 8,
            )

            Spacer(Modifier.height(20.dp))

            // Check button
            Button(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        analysisResult = RiskEngine.analyzeMessage(messageInput.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MessageShieldColor,
                ),
                enabled = messageInput.isNotBlank(),
            ) {
                Text(
                    text = "🔍  Check This Message",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                )
            }

            // Show result inline
            analysisResult?.let { result ->
                Spacer(Modifier.height(24.dp))
                MessageResultCard(result)
                Spacer(Modifier.height(14.dp))
                VoiceGuidanceCard(result = result, contextType = "message")
            }

            Spacer(Modifier.height(16.dp))

            // Example messages for demo
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Try these examples:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))

                    ExampleMessage(
                        label = "Fake bank SMS",
                        text = "SBI: Your account will be blocked today. Complete KYC immediately: bit.ly/sbi-kyc. Share OTP to verify.",
                    ) { messageInput = it }

                    ExampleMessage(
                        label = "Prize scam",
                        text = "Congratulations! You won ₹50,00,000 in lottery. Click here to claim: http://192.168.1.1/prize. Send ₹500 processing fee via UPI.",
                    ) { messageInput = it }

                    ExampleMessage(
                        label = "Normal message",
                        text = "Hey, are you coming for dinner tonight? Let me know by 7pm.",
                    ) { messageInput = it }
                }
            }
        }
    }
}

// ── Notification Listener Setup Card ────────────────────────────────────

@Composable
private fun NotificationListenerSetupCard(
    isEnabled: Boolean,
    onEnable: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isEnabled) SafeGreenLight else SurakshaPrimaryLight.copy(alpha = 0.1f),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (isEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✅", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Message Monitoring is ACTIVE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeGreen,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Suraksha Saathi is automatically watching for " +
                        "suspicious SMS and WhatsApp messages. You will be " +
                        "warned if a dangerous message is detected.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SafeGreen.copy(alpha = 0.85f),
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔒", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Enable Automatic Message Monitoring",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SurakshaPrimary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Allow Suraksha Saathi to read incoming message " +
                        "notifications and warn you automatically when a " +
                        "suspicious SMS or WhatsApp message arrives.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "🔒 Privacy: all analysis happens on your phone. " +
                        "Messages are never stored or uploaded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MessageShieldColor,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onEnable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MessageShieldColor,
                    ),
                ) {
                    Text(
                        text = "🛡️  Open Notification Access Settings",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Find \"Suraksha Saathi\" in the list and turn it ON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── Example Message Chip ────────────────────────────────────────────────

@Composable
private fun ExampleMessage(
    label: String,
    text: String,
    onClick: (String) -> Unit,
) {
    TextButton(
        onClick = { onClick(text) },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "📋 $label",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MessageShieldColor,
            )
            Text(
                text = text.take(60) + "...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

// ── Message Result Card ─────────────────────────────────────────────────

@Composable
private fun MessageResultCard(result: RiskResult) {
    val (bgColor, textColor, icon) = when (result.verdict) {
        RiskVerdict.STOP -> Triple(StopRedLight, StopRed, "🛑")
        RiskVerdict.VERIFY -> Triple(VerifyAmberLight, VerifyAmber, "⚠️")
        RiskVerdict.PROCEED -> Triple(SafeGreenLight, SafeGreen, "✅")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(icon, fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = result.verdict.label,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )

            if (result.evidenceList.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Why?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                result.evidenceList.forEach { evidence ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Text("•  ", color = textColor, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = evidence.indicator,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor.copy(alpha = 0.85f),
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "This message looks safe, but always be careful with unknown senders.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────

/**
 * Check whether Suraksha Saathi has been granted Notification Listener access.
 */
private fun isNotificationListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
    ) ?: return false
    return flat.contains(context.packageName)
}
