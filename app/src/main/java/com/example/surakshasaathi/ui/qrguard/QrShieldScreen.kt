package com.example.surakshasaathi.ui.qrguard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
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
import com.example.surakshasaathi.riskengine.ParsedUpiDetails
import com.example.surakshasaathi.riskengine.QrRiskEngine
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.riskengine.RiskVerdict
import com.example.surakshasaathi.theme.*
import com.example.surakshasaathi.voice.VoiceGuidanceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrShieldScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var qrInput by remember { mutableStateOf("") }
    var toldToReceiveMoney by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<RiskResult?>(null) }
    var parsedDetails by remember { mutableStateOf<ParsedUpiDetails?>(null) }

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
                    "QR & Payment Protection",
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
            // Header icon badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(QrShieldColor, QrShieldColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("📷", fontSize = 36.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Check QR & UPI Payment",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Paste or enter a UPI QR code string or payment ID before paying.\nWe will verify if it looks like a scam.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // Critical Senior Check: "Did someone say you will RECEIVE money?"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (toldToReceiveMoney) StopRedLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (toldToReceiveMoney) androidx.compose.foundation.BorderStroke(1.5.dp, StopRed) else null,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "❓ Important Question:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (toldToReceiveMoney) StopRed else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Did someone on a call or message tell you to scan this to RECEIVE or GET money?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        FilterChip(
                            selected = toldToReceiveMoney,
                            onClick = {
                                toldToReceiveMoney = true
                                if (qrInput.isNotBlank()) {
                                    val parsed = QrRiskEngine.parseQrPayload(qrInput)
                                    parsedDetails = parsed
                                    analysisResult = QrRiskEngine.analyzePayment(parsed, true)
                                }
                            },
                            label = {
                                Text(
                                    "Yes, to receive money",
                                    fontWeight = if (toldToReceiveMoney) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StopRed,
                                selectedLabelColor = Color.White,
                            ),
                            modifier = Modifier.weight(1f),
                        )

                        FilterChip(
                            selected = !toldToReceiveMoney,
                            onClick = {
                                toldToReceiveMoney = false
                                if (qrInput.isNotBlank()) {
                                    val parsed = QrRiskEngine.parseQrPayload(qrInput)
                                    parsedDetails = parsed
                                    analysisResult = QrRiskEngine.analyzePayment(parsed, false)
                                }
                            },
                            label = {
                                Text("No, I am paying")
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // QR / UPI Input
            OutlinedTextField(
                value = qrInput,
                onValueChange = {
                    qrInput = it
                    analysisResult = null
                    parsedDetails = null
                },
                label = { Text("Paste UPI / QR link or VPA", style = MaterialTheme.typography.bodyLarge) },
                placeholder = { Text("upi://pay?pa=name@bank&pn=Shop...") },
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
                    if (qrInput.isNotBlank()) {
                        val parsed = QrRiskEngine.parseQrPayload(qrInput.trim())
                        parsedDetails = parsed
                        analysisResult = QrRiskEngine.analyzePayment(parsed, toldToReceiveMoney)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QrShieldColor,
                ),
                enabled = qrInput.isNotBlank(),
            ) {
                Text(
                    text = "🔍  Check This Payment",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                )
            }

            // Results Section
            analysisResult?.let { result ->
                Spacer(Modifier.height(24.dp))
                QrResultCard(result = result, details = parsedDetails)
                Spacer(Modifier.height(14.dp))
                VoiceGuidanceCard(result = result, contextType = "payment request")
            }

            Spacer(Modifier.height(24.dp))

            // Golden Rules for UPI Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SafeGreenLight,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "💡 The Golden Rules of UPI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeGreen,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "1. You NEVER enter your UPI PIN to receive money.\n" +
                            "2. Entering a PIN always DEBITS money from your bank account.\n" +
                            "3. You never scan a QR code to receive a lottery, prize, or refund.\n" +
                            "4. If a caller says \"just enter your PIN to accept payment\", HANG UP immediately.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SafeGreen.copy(alpha = 0.9f),
                        lineHeight = 24.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Demo Examples Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Try these demo payment QRs:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))

                    ExamplePaymentButton(
                        title = "⚠️ Fake Electricity Refund (Claims refund, charges ₹2,499)",
                        payload = "upi://pay?pa=wbseb.refund88@okaxis&pn=Electricity+Department&am=2499.00&tn=Refund+Clearance",
                        isReceive = true,
                    ) { payload, isRec ->
                        qrInput = payload
                        toldToReceiveMoney = isRec
                    }

                    ExamplePaymentButton(
                        title = "⚠️ Lottery Prize Fee Scam (Asks ₹1,500 fee)",
                        payload = "upi://pay?pa=kbc.lotterywinner@paytm&pn=Kaun+Banega+Winner&am=1500.00&tn=Registration+Fee",
                        isReceive = true,
                    ) { payload, isRec ->
                        qrInput = payload
                        toldToReceiveMoney = isRec
                    }

                    ExamplePaymentButton(
                        title = "✅ Genuine Local Store QR (Corner Grocery)",
                        payload = "upi://pay?pa=sharmagrocery@icici&pn=Sharma+General+Store&am=250.00",
                        isReceive = false,
                    ) { payload, isRec ->
                        qrInput = payload
                        toldToReceiveMoney = isRec
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ExamplePaymentButton(
    title: String,
    payload: String,
    isReceive: Boolean,
    onClick: (String, Boolean) -> Unit,
) {
    TextButton(
        onClick = { onClick(payload, isReceive) },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = QrShieldColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QrResultCard(
    result: RiskResult,
    details: ParsedUpiDetails?,
) {
    val (bgColor, textColor, icon) = when (result.verdict) {
        RiskVerdict.STOP -> Triple(StopRedLight, StopRed, "🛑")
        RiskVerdict.VERIFY -> Triple(VerifyAmberLight, VerifyAmber, "⚠️")
        RiskVerdict.PROCEED -> Triple(SafeGreenLight, SafeGreen, "✅")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(icon, fontSize = 42.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = result.verdict.label,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )

            // Parsed UPI Details Box
            if (details != null && details.isUpiPayment) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.7f),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (details.payeeName.isNotBlank()) {
                            Text(
                                text = "👤 Payee Name: ${details.payeeName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight,
                            )
                        }
                        if (details.payeeVpa.isNotBlank()) {
                            Text(
                                text = "💳 Payee VPA: ${details.payeeVpa}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight,
                            )
                        }
                        if (!details.amount.isNullOrBlank()) {
                            Text(
                                text = "💰 Amount: ₹${details.amount}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StopRed,
                            )
                        }
                        if (!details.note.isNullOrBlank()) {
                            Text(
                                text = "📝 Note: ${details.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Evidence list
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
                        color = textColor.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}
