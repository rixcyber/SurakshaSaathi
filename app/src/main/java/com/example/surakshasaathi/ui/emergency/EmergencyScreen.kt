package com.example.surakshasaathi.ui.emergency

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surakshasaathi.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

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
                    "Fraud Help",
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
            // Emergency header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = StopRedLight,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🚨", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "I Think I Have Been Scammed",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = StopRed,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Do not panic. Follow these steps.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = StopRed.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Step-by-step guidance
            EmergencyStep(
                number = "1",
                title = "Stop Further Payments",
                description = "Do not send any more money. If a payment is in progress, try to cancel it.",
                emoji = "🛑",
            )

            Spacer(Modifier.height(14.dp))

            EmergencyStep(
                number = "2",
                title = "Contact Your Bank Immediately",
                description = "Call your bank's customer care number on the back of your card. Tell them about the fraud.",
                emoji = "🏦",
            )

            Spacer(Modifier.height(14.dp))

            EmergencyStep(
                number = "3",
                title = "Block the Affected Card/Account",
                description = "Ask your bank to block the card or account if required to prevent further transactions.",
                emoji = "🔒",
            )

            Spacer(Modifier.height(14.dp))

            EmergencyStep(
                number = "4",
                title = "Call Cyber-Fraud Helpline 1930",
                description = "This is the government helpline for reporting cyber fraud. The earlier you call, the better the chance of recovery.",
                emoji = "📞",
            )

            Spacer(Modifier.height(20.dp))

            // Call 1930 button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:1930")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StopRed,
                ),
            ) {
                Text(
                    text = "📞  Call 1930 — Cyber Fraud Helpline",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(14.dp))

            EmergencyStep(
                number = "5",
                title = "Preserve Evidence",
                description = "Keep screenshots of messages, payment details, and call logs. You will need these for the police report.",
                emoji = "📸",
            )

            Spacer(Modifier.height(24.dp))

            // Important notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = VerifyAmberLight,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️  Important",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VerifyAmber,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "This app cannot reverse a payment or recover money. Only your bank and the authorities can help with that. The earlier you report, the better your chances.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VerifyAmber.copy(alpha = 0.85f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // File online complaint
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://cybercrime.gov.in")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SurakshaPrimary,
                ),
            ) {
                Text(
                    text = "🌐  File Online Complaint (cybercrime.gov.in)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EmergencyStep(
    number: String,
    title: String,
    description: String,
    emoji: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Step number badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(StopRed, StopRed.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
