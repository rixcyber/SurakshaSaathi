package com.example.surakshasaathi.ui.linkguard

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surakshasaathi.riskengine.RiskEngine
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.riskengine.RiskVerdict
import com.example.surakshasaathi.theme.*
import com.example.surakshasaathi.voice.VoiceGuidanceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkShieldScreen(
    onBack: () -> Unit,
    onNavigateToResult: (RiskResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    var urlInput by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<RiskResult?>(null) }

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
                    "Link Protection",
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
                            listOf(LinkShieldColor, LinkShieldColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("🔗", fontSize = 36.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Check a Link",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Paste or type a link below.\nWe will check if it is safe.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // URL input
            OutlinedTextField(
                value = urlInput,
                onValueChange = {
                    urlInput = it
                    analysisResult = null
                },
                label = { Text("Paste link here", style = MaterialTheme.typography.bodyLarge) },
                placeholder = { Text("https://example.com") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(14.dp),
                singleLine = false,
                maxLines = 3,
            )

            Spacer(Modifier.height(20.dp))

            // Check button
            Button(
                onClick = {
                    if (urlInput.isNotBlank()) {
                        analysisResult = RiskEngine.analyzeLink(urlInput.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LinkShieldColor,
                ),
                enabled = urlInput.isNotBlank(),
            ) {
                Text(
                    text = "🔍  Check This Link",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                )
            }

            // Show result inline
            analysisResult?.let { result ->
                Spacer(Modifier.height(24.dp))
                LinkResultCard(result)
                Spacer(Modifier.height(14.dp))
                VoiceGuidanceCard(result = result, contextType = "link")
            }

            Spacer(Modifier.height(16.dp))

            // Example suspicious links for demo
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
                    ExampleChip("sbi-login-verification.xyz") { urlInput = it }
                    ExampleChip("http://192.168.1.100/login") { urlInput = it }
                    ExampleChip("bit.ly/free-prize-win") { urlInput = it }
                    ExampleChip("https://www.sbi.co.in") { urlInput = it }
                }
            }
        }
    }
}

@Composable
private fun ExampleChip(text: String, onClick: (String) -> Unit) {
    TextButton(
        onClick = { onClick(text) },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = LinkShieldColor,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LinkResultCard(result: RiskResult) {
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
            Spacer(Modifier.height(12.dp))

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
        }
    }
}
