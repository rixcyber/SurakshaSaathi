package com.example.surakshasaathi.ui.callguard

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.surakshasaathi.riskengine.LocalReputationDb
import com.example.surakshasaathi.riskengine.RiskEngine
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.riskengine.RiskVerdict
import com.example.surakshasaathi.theme.*
import com.example.surakshasaathi.voice.VoiceGuidanceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallShieldScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var phoneInput by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<RiskResult?>(null) }

    // ── Call-screening role state ──
    var hasCallScreeningRole by remember { mutableStateOf(false) }

    // Recheck the role every time the screen is resumed (e.g. after
    // returning from the system role-grant dialog)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCallScreeningRole = isCallScreeningRoleHeld(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Launcher for the system role-request dialog
    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        // Recheck after the dialog closes
        hasCallScreeningRole = isCallScreeningRoleHeld(context)
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
                    "Call Protection",
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
                            listOf(CallShieldColor, CallShieldColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("📞", fontSize = 36.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Check a Phone Number",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Enter the phone number that called you.\nWe will check if it has been reported.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // ── Call Screening Service Setup Card ──
            CallScreeningSetupCard(
                isEnabled = hasCallScreeningRole,
                onEnable = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val rm = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                        val intent = rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                        roleRequestLauncher.launch(intent)
                    }
                },
            )

            Spacer(Modifier.height(24.dp))

            // Phone number input
            OutlinedTextField(
                value = phoneInput,
                onValueChange = {
                    phoneInput = it
                    analysisResult = null
                },
                label = { Text("Phone number", style = MaterialTheme.typography.bodyLarge) },
                placeholder = { Text("+91 XXXXX XXXXX") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Spacer(Modifier.height(20.dp))

            // Check button
            Button(
                onClick = {
                    if (phoneInput.isNotBlank()) {
                        analysisResult = RiskEngine.analyzePhoneNumber(phoneInput.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CallShieldColor,
                ),
                enabled = phoneInput.isNotBlank(),
            ) {
                Text(
                    text = "🔍  Check This Number",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                )
            }

            // Show result inline
            analysisResult?.let { result ->
                Spacer(Modifier.height(24.dp))
                CallResultCard(result)
                Spacer(Modifier.height(14.dp))
                VoiceGuidanceCard(result = result, contextType = "call")
            }

            Spacer(Modifier.height(24.dp))

            // Demo numbers
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Try these demo numbers:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))

                    LocalReputationDb.getAllKnownScamNumbers().forEach { entry ->
                        TextButton(
                            onClick = { phoneInput = entry.number },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "📋 ${entry.number}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CallShieldColor,
                                )
                                Text(
                                    text = entry.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Call Screening Setup Card ───────────────────────────────────────────

@Composable
private fun CallScreeningSetupCard(
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
                        text = "Call Screening is ACTIVE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeGreen,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Suraksha Saathi is automatically screening incoming calls " +
                        "and will warn you about suspicious numbers before you pick up.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SafeGreen.copy(alpha = 0.85f),
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔒", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Enable Automatic Call Screening",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SurakshaPrimary,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Allow Suraksha Saathi to check every incoming call " +
                        "automatically and warn you about scam callers.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Button(
                        onClick = onEnable,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CallShieldColor,
                        ),
                    ) {
                        Text(
                            text = "🛡️  Enable Call Screening",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 16.sp,
                        )
                    }
                } else {
                    Text(
                        text = "Automatic call screening requires Android 10 or later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── Call Result Card ────────────────────────────────────────────────────

@Composable
private fun CallResultCard(result: RiskResult) {
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
                    text = "This number has not been reported.\nBut be careful with unknown callers.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────

private fun isCallScreeningRoleHeld(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    val rm = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    return rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
}
